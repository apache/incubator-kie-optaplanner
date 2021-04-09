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

package org.optaplanner.core.impl.score.buildin.hardmediumsoftlong;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.impl.score.inliner.JustificationsSupplier;
import org.optaplanner.core.impl.score.inliner.LongWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardMediumSoftLongScoreInliner extends ScoreInliner<HardMediumSoftLongScore> {

    private long hardScore;
    private long mediumScore;
    private long softScore;

    protected HardMediumSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardMediumSoftLongScore.ZERO);
    }

    @Override
    public LongWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            HardMediumSoftLongScore constraintWeight) {
        assertNonZeroConstraintWeight(constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long mediumConstraintWeight = constraintWeight.getMediumScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight == 0L && softConstraintWeight == 0L) {
            return (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                long hardImpact = hardConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.hardScore -= hardImpact,
                        () -> HardMediumSoftLongScore.ofHard(hardImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight == 0L && softConstraintWeight == 0L) {
            return (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                long mediumImpact = mediumConstraintWeight * matchWeight;
                this.mediumScore += mediumImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.mediumScore -= mediumImpact,
                        () -> HardMediumSoftLongScore.ofMedium(mediumImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight == 0L && mediumConstraintWeight == 0L) {
            return (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                long softImpact = softConstraintWeight * matchWeight;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.softScore -= softImpact,
                        () -> HardMediumSoftLongScore.ofSoft(softImpact),
                        justificationsSupplier);
            };
        } else {
            return (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                long hardImpact = hardConstraintWeight * matchWeight;
                long mediumImpact = mediumConstraintWeight * matchWeight;
                long softImpact = softConstraintWeight * matchWeight;
                this.hardScore += hardImpact;
                this.mediumScore += mediumImpact;
                this.softScore += softImpact;
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        },
                        () -> HardMediumSoftLongScore.of(hardImpact, mediumImpact, softImpact),
                        justificationsSupplier);
            };
        }
    }

    @Override
    public HardMediumSoftLongScore extractScore(int initScore) {
        return HardMediumSoftLongScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftLongScore.class.getSimpleName() + " inliner";
    }

}
