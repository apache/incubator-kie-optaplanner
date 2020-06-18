/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.definition.AbstractScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class HardSoftBigDecimalScoreDefinition extends AbstractScoreDefinition<HardSoftBigDecimalScore> {

    public HardSoftBigDecimalScoreDefinition() {
        super(new String[] { "hard score", "soft score" });
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
        return 2;
    }

    @Override
    public int getFeasibleLevelsSize() {
        return 1;
    }

    @Override
    public Class<HardSoftBigDecimalScore> getScoreClass() {
        return HardSoftBigDecimalScore.class;
    }

    @Override
    public HardSoftBigDecimalScore getZeroScore() {
        return HardSoftBigDecimalScore.ZERO;
    }

    @Override
    public HardSoftBigDecimalScore getOneSoftestScore() {
        return HardSoftBigDecimalScore.ONE_SOFT;
    }

    @Override
    public HardSoftBigDecimalScore parseScore(String scoreString) {
        return HardSoftBigDecimalScore.parseScore(scoreString);
    }

    @Override
    public HardSoftBigDecimalScore fromLevelNumbers(int initScore, Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        return HardSoftBigDecimalScore.ofUninitialized(initScore, (BigDecimal) levelNumbers[0], (BigDecimal) levelNumbers[1]);
    }

    @Override
    public HardSoftBigDecimalScoreInliner buildScoreInliner(boolean constraintMatchEnabled) {
        return new HardSoftBigDecimalScoreInliner(constraintMatchEnabled);
    }

    @Override
    public HardSoftBigDecimalScoreHolderImpl buildScoreHolder(boolean constraintMatchEnabled) {
        return new HardSoftBigDecimalScoreHolderImpl(constraintMatchEnabled);
    }

    @Override
    public HardSoftBigDecimalScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend,
            HardSoftBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public HardSoftBigDecimalScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend,
            HardSoftBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public HardSoftBigDecimalScore divideBySanitizedDivisor(HardSoftBigDecimalScore dividend,
            HardSoftBigDecimalScore divisor) {
        int dividendInitScore = dividend.getInitScore();
        int divisorInitScore = sanitize(divisor.getInitScore());
        BigDecimal dividendHardScore = dividend.getHardScore();
        BigDecimal divisorHardScore = sanitize(divisor.getHardScore());
        BigDecimal dividendSoftScore = dividend.getSoftScore();
        BigDecimal divisorSoftScore = sanitize(divisor.getSoftScore());
        return fromLevelNumbers(
                divide(dividendInitScore, divisorInitScore),
                new Number[] {
                        divide(dividendHardScore, divisorHardScore),
                        divide(dividendSoftScore, divisorSoftScore)
                });
    }
}
