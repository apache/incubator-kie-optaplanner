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

package org.optaplanner.core.impl.score.buildin.simplebigdecimal;

import java.math.BigDecimal;
import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.impl.score.definition.AbstractScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class SimpleBigDecimalScoreDefinition extends AbstractScoreDefinition<SimpleBigDecimalScore> {

    public SimpleBigDecimalScoreDefinition() {
        super(new String[] { "score" });
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
        return 1;
    }

    @Override
    public int getFeasibleLevelsSize() {
        return 0;
    }

    @Override
    public Class<SimpleBigDecimalScore> getScoreClass() {
        return SimpleBigDecimalScore.class;
    }

    @Override
    public SimpleBigDecimalScore getZeroScore() {
        return SimpleBigDecimalScore.ZERO;
    }

    @Override
    public SimpleBigDecimalScore getOneSoftestScore() {
        return SimpleBigDecimalScore.ONE;
    }

    @Override
    public SimpleBigDecimalScore parseScore(String scoreString) {
        return SimpleBigDecimalScore.parseScore(scoreString);
    }

    @Override
    public SimpleBigDecimalScore fromLevelNumbers(int initScore, Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        return SimpleBigDecimalScore.ofUninitialized(initScore, (BigDecimal) levelNumbers[0]);
    }

    @Override
    public SimpleBigDecimalScoreInliner buildScoreInliner(boolean constraintMatchEnabled) {
        return new SimpleBigDecimalScoreInliner(constraintMatchEnabled);
    }

    @Override
    public SimpleBigDecimalScoreHolderImpl buildScoreHolder(boolean constraintMatchEnabled) {
        return new SimpleBigDecimalScoreHolderImpl(constraintMatchEnabled);
    }

    @Override
    public SimpleBigDecimalScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend,
            SimpleBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public SimpleBigDecimalScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend,
            SimpleBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public SimpleBigDecimalScore divideBySanitizedDivisor(SimpleBigDecimalScore dividend,
            SimpleBigDecimalScore divisor) {
        int dividendInitScore = dividend.getInitScore();
        int divisorInitScore = sanitize(divisor.getInitScore());
        BigDecimal dividendScore = dividend.getScore();
        BigDecimal divisorScore = sanitize(divisor.getScore());
        return fromLevelNumbers(
                divide(dividendInitScore, divisorInitScore),
                new Number[] {
                        divide(dividendScore, divisorScore)
                });
    }
}
