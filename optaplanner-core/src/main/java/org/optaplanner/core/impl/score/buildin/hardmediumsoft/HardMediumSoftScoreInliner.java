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

package org.optaplanner.core.impl.score.buildin.hardmediumsoft;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.inliner.IntWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.JustificationsSupplier;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardMediumSoftScoreInliner extends ScoreInliner<HardMediumSoftScore> {

    private int hardScore;
    private int mediumScore;
    private int softScore;

    protected HardMediumSoftScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardMediumSoftScore.ZERO);
    }

    @Override
    public IntWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            HardMediumSoftScore constraintWeight) {
        assertNonZeroConstraintWeight(constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int mediumConstraintWeight = constraintWeight.getMediumScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight == 0 && softConstraintWeight == 0) {
            return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                int hardImpact = hardConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.hardScore -= hardImpact,
                        () -> HardMediumSoftScore.ofHard(hardImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight == 0 && softConstraintWeight == 0) {
            return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                int mediumImpact = mediumConstraintWeight * matchWeight;
                this.mediumScore += mediumImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.mediumScore -= mediumImpact,
                        () -> HardMediumSoftScore.ofMedium(mediumImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight == 0 && mediumConstraintWeight == 0) {
            return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                int softImpact = softConstraintWeight * matchWeight;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.softScore -= softImpact,
                        () -> HardMediumSoftScore.ofSoft(softImpact),
                        justificationsSupplier);
            };
        } else {
            return (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                int hardImpact = hardConstraintWeight * matchWeight;
                int mediumImpact = mediumConstraintWeight * matchWeight;
                int softImpact = softConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                this.mediumScore += mediumImpact;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        },
                        () -> HardMediumSoftScore.of(hardImpact, mediumImpact, softImpact),
                        justificationsSupplier);
            };
        }
    }

    @Override
    public HardMediumSoftScore extractScore(int initScore) {
        return HardMediumSoftScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftScore.class.getSimpleName() + " inliner";
    }

}
