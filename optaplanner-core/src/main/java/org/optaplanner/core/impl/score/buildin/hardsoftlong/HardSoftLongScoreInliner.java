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

package org.optaplanner.core.impl.score.buildin.hardsoftlong;

import java.util.List;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.inliner.LongWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardSoftLongScoreInliner extends ScoreInliner<HardSoftLongScore> {

    private long hardScore;
    private long softScore;

    protected HardSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardSoftLongScore.ZERO);
    }

    @Override
    public LongWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            HardSoftLongScore constraintWeight) {
        ensureNonZeroConstraintWeight(constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight == 0L) {
            return (long matchWeight, Supplier<List<Object>> justifications) -> {
                long hardImpact = hardConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.hardScore -= hardImpact,
                        () -> HardSoftLongScore.ofHard(hardImpact),
                        justifications);
            };
        } else if (hardConstraintWeight == 0L) {
            return (long matchWeight, Supplier<List<Object>> justifications) -> {
                long softImpact = softConstraintWeight * matchWeight;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.softScore -= softImpact,
                        () -> HardSoftLongScore.ofSoft(softImpact),
                        justifications);
            };
        } else {
            return (long matchWeight, Supplier<List<Object>> justifications) -> {
                long hardImpact = hardConstraintWeight * matchWeight;
                long softImpact = softConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        },
                        () -> HardSoftLongScore.of(hardImpact, softImpact),
                        justifications);
            };
        }
    }

    @Override
    public HardSoftLongScore extractScore(int initScore) {
        return HardSoftLongScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftLongScore.class.getSimpleName() + " inliner";
    }

}
