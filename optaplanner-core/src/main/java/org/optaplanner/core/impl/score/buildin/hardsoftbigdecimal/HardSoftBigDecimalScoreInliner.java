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

package org.optaplanner.core.impl.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.inliner.BigDecimalWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardSoftBigDecimalScoreInliner extends ScoreInliner<HardSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    protected HardSoftBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardSoftBigDecimalScore.ZERO);
    }

    @Override
    public BigDecimalWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName, HardSoftBigDecimalScore constraintWeight) {
        ensureNonZeroConstraintWeight(constraintWeight);
        BigDecimal hardConstraintWeight = constraintWeight.getHardScore();
        BigDecimal softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight.equals(BigDecimal.ZERO)) {
            return (BigDecimal matchWeight, Consumer<Score<?>> matchScoreConsumer) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                if (constraintMatchEnabled) {
                    matchScoreConsumer.accept(HardSoftBigDecimalScore.ofHard(hardImpact));
                }
                return () -> this.hardScore = this.hardScore.subtract(hardImpact);
            };
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO)) {
            return (BigDecimal matchWeight, Consumer<Score<?>> matchScoreConsumer) -> {
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.softScore = this.softScore.add(softImpact);
                if (constraintMatchEnabled) {
                    matchScoreConsumer.accept(HardSoftBigDecimalScore.ofSoft(softImpact));
                }
                return () -> this.softScore = this.softScore.subtract(softImpact);
            };
        } else {
            return (BigDecimal matchWeight, Consumer<Score<?>> matchScoreConsumer) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                this.softScore = this.softScore.add(softImpact);
                if (constraintMatchEnabled) {
                    matchScoreConsumer.accept(HardSoftBigDecimalScore.of(hardImpact, softImpact));
                }
                return () -> {
                    this.hardScore = this.hardScore.subtract(hardImpact);
                    this.softScore = this.softScore.subtract(softImpact);
                };
            };
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
