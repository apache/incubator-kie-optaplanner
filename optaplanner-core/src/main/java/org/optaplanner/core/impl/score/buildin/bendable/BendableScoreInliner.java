/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.buildin.bendable;

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.inliner.IntWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.JustificationsSupplier;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;
import org.optaplanner.core.impl.score.inliner.UndoScoreImpacter;

public final class BendableScoreInliner extends ScoreInliner<BendableScore> {

    private final int[] hardScores;
    private final int[] softScores;

    public BendableScoreInliner(boolean constraintMatchEnabled, int hardLevelsSize, int softLevelsSize) {
        super(constraintMatchEnabled, BendableScore.zero(hardLevelsSize, softLevelsSize));
        hardScores = new int[hardLevelsSize];
        softScores = new int[softLevelsSize];
    }

    @Override
    public IntWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            BendableScore constraintWeight) {
        assertNonZeroConstraintWeight(constraintWeight);
        String constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName); // Cache.
        Integer singleLevel = null;
        for (int i = 0; i < constraintWeight.getLevelsSize(); i++) {
            if (constraintWeight.getHardOrSoftScore(i) != 0L) {
                if (singleLevel != null) {
                    singleLevel = null;
                    break;
                }
                singleLevel = i;
            }
        }
        if (singleLevel != null) {
            int levelWeight = constraintWeight.getHardOrSoftScore(singleLevel);
            if (singleLevel < constraintWeight.getHardLevelsSize()) {
                int level = singleLevel;
                return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    int hardImpact = levelWeight * matchWeight;
                    this.hardScores[level] += hardImpact;
                    UndoScoreImpacter undoScoreImpact = () -> this.hardScores[level] -= hardImpact;
                    if (!constraintMatchEnabled) {
                        return undoScoreImpact;
                    }
                    Runnable undoConstraintMatch = addConstraintMatch(constraintId, constraintPackage, constraintName,
                            constraintWeight,
                            BendableScore.ofHard(hardScores.length, softScores.length, level, hardImpact),
                            justificationsSupplier.get());
                    return () -> {
                        undoScoreImpact.run();
                        undoConstraintMatch.run();
                    };
                };
            } else {
                int level = singleLevel - constraintWeight.getHardLevelsSize();
                return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    int softImpact = levelWeight * matchWeight;
                    this.softScores[level] += softImpact;
                    UndoScoreImpacter undoScoreImpact = () -> this.softScores[level] -= softImpact;
                    if (!constraintMatchEnabled) {
                        return undoScoreImpact;
                    }
                    Runnable undoConstraintMatch = addConstraintMatch(constraintId, constraintPackage, constraintName,
                            constraintWeight,
                            BendableScore.ofSoft(hardScores.length, softScores.length, level, softImpact),
                            justificationsSupplier.get());
                    return () -> {
                        undoScoreImpact.run();
                        undoConstraintMatch.run();
                    };
                };
            }
        } else {
            return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                int[] hardImpacts = new int[hardScores.length];
                int[] softImpacts = new int[softScores.length];
                for (int i = 0; i < hardImpacts.length; i++) {
                    hardImpacts[i] = constraintWeight.getHardScore(i) * matchWeight;
                    this.hardScores[i] += hardImpacts[i];
                }
                for (int i = 0; i < softImpacts.length; i++) {
                    softImpacts[i] = constraintWeight.getSoftScore(i) * matchWeight;
                    this.softScores[i] += softImpacts[i];
                }
                UndoScoreImpacter undoScoreImpact = () -> {
                    for (int i = 0; i < hardImpacts.length; i++) {
                        this.hardScores[i] -= hardImpacts[i];
                    }
                    for (int i = 0; i < softImpacts.length; i++) {
                        this.softScores[i] -= softImpacts[i];
                    }
                };
                if (!constraintMatchEnabled) {
                    return undoScoreImpact;
                }
                Runnable undoConstraintMatch = addConstraintMatch(constraintId, constraintPackage, constraintName,
                        constraintWeight, BendableScore.of(hardImpacts, softImpacts), justificationsSupplier.get());
                return () -> {
                    undoScoreImpact.run();
                    undoConstraintMatch.run();
                };
            };
        }
    }

    @Override
    public BendableScore extractScore(int initScore) {
        return BendableScore.ofUninitialized(initScore,
                Arrays.copyOf(hardScores, hardScores.length),
                Arrays.copyOf(softScores, softScores.length));
    }

    @Override
    public String toString() {
        return BendableScore.class.getSimpleName() + " inliner";
    }

}
