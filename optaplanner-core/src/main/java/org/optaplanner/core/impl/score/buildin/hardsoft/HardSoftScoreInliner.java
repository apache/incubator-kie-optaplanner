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

package org.optaplanner.core.impl.score.buildin.hardsoft;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.inliner.IntWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardSoftScoreInliner extends ScoreInliner<HardSoftScore> {

    private int hardScore;
    private int softScore;

    protected HardSoftScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardSoftScore.ZERO);
    }

    @Override
    public IntWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            HardSoftScore constraintWeight) {
        ensureNonZeroConstraintWeight(constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight == 0) {
            return (int matchWeight, Object... justifications) -> {
                int hardImpact = hardConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                return buildUndo(constraintPackage, constraintName,
                        () -> this.hardScore -= hardImpact,
                        () -> HardSoftScore.ofHard(hardImpact),
                        justifications);
            };
        } else if (hardConstraintWeight == 0) {
            return (int matchWeight, Object... justifications) -> {
                int softImpact = softConstraintWeight * matchWeight;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName,
                        () -> this.softScore -= softImpact,
                        () -> HardSoftScore.ofSoft(softImpact),
                        justifications);
            };
        } else {
            return (int matchWeight, Object... justifications) -> {
                int hardImpact = hardConstraintWeight * matchWeight;
                int softImpact = softConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName,
                        () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        },
                        () -> HardSoftScore.of(hardImpact, softImpact),
                        justifications);
            };
        }
    }

    @Override
    public HardSoftScore extractScore(int initScore) {
        return HardSoftScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftScore.class.getSimpleName() + " inliner";
    }

}
