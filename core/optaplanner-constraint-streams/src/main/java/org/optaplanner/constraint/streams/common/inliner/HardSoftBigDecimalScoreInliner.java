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

package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftBigDecimalScoreInliner extends AbstractScoreInliner<HardSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    HardSoftBigDecimalScoreInliner(Map<Constraint, HardSoftBigDecimalScore> constraintToWeightMap,
            boolean constraintMatchEnabled) {
        super(constraintToWeightMap, constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint) {
        HardSoftBigDecimalScore constraintWeight = getConstraintWeight(constraint);
        BigDecimal hardConstraintWeight = constraintWeight.getHardScore();
        BigDecimal softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of((BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                UndoScoreImpacter undoScoreImpact = () -> this.hardScore = this.hardScore.subtract(hardImpact);
                if (!constraintMatchEnabled) {
                    return undoScoreImpact;
                }
                Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                        HardSoftBigDecimalScore.ofHard(hardImpact), justificationsSupplier.get());
                return () -> {
                    undoScoreImpact.run();
                    undoConstraintMatch.run();
                };
            });
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of((BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.softScore = this.softScore.add(softImpact);
                UndoScoreImpacter undoScoreImpact = () -> this.softScore = this.softScore.subtract(softImpact);
                if (!constraintMatchEnabled) {
                    return undoScoreImpact;
                }
                Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                        HardSoftBigDecimalScore.ofSoft(softImpact), justificationsSupplier.get());
                return () -> {
                    undoScoreImpact.run();
                    undoConstraintMatch.run();
                };
            });
        } else {
            return WeightedScoreImpacter.of((BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                this.softScore = this.softScore.add(softImpact);
                UndoScoreImpacter undoScoreImpact = () -> {
                    this.hardScore = this.hardScore.subtract(hardImpact);
                    this.softScore = this.softScore.subtract(softImpact);
                };
                if (!constraintMatchEnabled) {
                    return undoScoreImpact;
                }
                Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                        HardSoftBigDecimalScore.of(hardImpact, softImpact),
                        justificationsSupplier.get());
                return () -> {
                    undoScoreImpact.run();
                    undoConstraintMatch.run();
                };
            });
        }
    }

    @Override
    public HardSoftBigDecimalScore extractScore(int initScore) {
        return HardSoftBigDecimalScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
