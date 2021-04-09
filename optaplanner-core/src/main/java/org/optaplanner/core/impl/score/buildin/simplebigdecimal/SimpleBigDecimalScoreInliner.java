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

package org.optaplanner.core.impl.score.buildin.simplebigdecimal;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.impl.score.inliner.BigDecimalWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.JustificationsSupplier;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class SimpleBigDecimalScoreInliner extends ScoreInliner<SimpleBigDecimalScore> {

    private BigDecimal score = BigDecimal.ZERO;

    protected SimpleBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, SimpleBigDecimalScore.ZERO);
    }

    @Override
    public BigDecimalWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            SimpleBigDecimalScore constraintWeight) {
        assertNonZeroConstraintWeight(constraintWeight);
        BigDecimal simpleConstraintWeight = constraintWeight.getScore();
        return (BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
            BigDecimal impact = simpleConstraintWeight.multiply(matchWeight);
            this.score = this.score.add(impact);
            return buildUndo(constraintPackage, constraintName, constraintWeight,
                    () -> this.score = this.score.subtract(impact),
                    () -> SimpleBigDecimalScore.of(impact),
                    justificationsSupplier);
        };
    }

    @Override
    public SimpleBigDecimalScore extractScore(int initScore) {
        return SimpleBigDecimalScore.ofUninitialized(initScore, score);
    }

    @Override
    public String toString() {
        return SimpleBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
