/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.score.buildin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.impl.score.definition.AbstractBendableScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class BendableBigDecimalScoreDefinition extends AbstractBendableScoreDefinition<BendableBigDecimalScore> {

    public BendableBigDecimalScoreDefinition(int hardLevelsSize, int softLevelsSize) {
        super(hardLevelsSize, softLevelsSize);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public Class<BendableBigDecimalScore> getScoreClass() {
        return BendableBigDecimalScore.class;
    }

    @Override
    public BendableBigDecimalScore getZeroScore() {
        return BendableBigDecimalScore.zero(hardLevelsSize, softLevelsSize);
    }

    @Override
    public final BendableBigDecimalScore getOneSoftestScore() {
        return BendableBigDecimalScore.ofSoft(hardLevelsSize, softLevelsSize, softLevelsSize - 1, BigDecimal.ONE);
    }

    @Override
    public BendableBigDecimalScore parseScore(String scoreString) {
        BendableBigDecimalScore score = BendableBigDecimalScore.parseScore(scoreString);
        if (score.hardLevelsSize() != hardLevelsSize) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + BendableBigDecimalScore.class.getSimpleName()
                    + ") doesn't follow the correct pattern:"
                    + " the hardLevelsSize (" + score.hardLevelsSize()
                    + ") doesn't match the scoreDefinition's hardLevelsSize (" + hardLevelsSize + ").");
        }
        if (score.softLevelsSize() != softLevelsSize) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + BendableBigDecimalScore.class.getSimpleName()
                    + ") doesn't follow the correct pattern:"
                    + " the softLevelsSize (" + score.softLevelsSize()
                    + ") doesn't match the scoreDefinition's softLevelsSize (" + softLevelsSize + ").");
        }
        return score;
    }

    @Override
    public BendableBigDecimalScore fromLevelNumbers(int initScore, Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        BigDecimal[] hardScores = new BigDecimal[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = (BigDecimal) levelNumbers[i];
        }
        BigDecimal[] softScores = new BigDecimal[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = (BigDecimal) levelNumbers[hardLevelsSize + i];
        }
        return BendableBigDecimalScore.ofUninitialized(initScore, hardScores, softScores);
    }

    public BendableBigDecimalScore createScore(BigDecimal... scores) {
        return createScoreUninitialized(0, scores);
    }

    public BendableBigDecimalScore createScoreUninitialized(int initScore, BigDecimal... scores) {
        int levelsSize = hardLevelsSize + softLevelsSize;
        if (scores.length != levelsSize) {
            throw new IllegalArgumentException("The scores (" + Arrays.toString(scores)
                    + ")'s length (" + scores.length
                    + ") is not levelsSize (" + levelsSize + ").");
        }
        return BendableBigDecimalScore.ofUninitialized(initScore,
                Arrays.copyOfRange(scores, 0, hardLevelsSize),
                Arrays.copyOfRange(scores, hardLevelsSize, levelsSize));
    }

    @Override
    public BendableBigDecimalScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend,
            BendableBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public BendableBigDecimalScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend,
            BendableBigDecimalScore score) {
        // TODO https://issues.redhat.com/browse/PLANNER-232
        throw new UnsupportedOperationException("PLANNER-232: BigDecimalScore does not support bounds" +
                " because a BigDecimal cannot represent infinity.");
    }

    @Override
    public BendableBigDecimalScore divideBySanitizedDivisor(BendableBigDecimalScore dividend,
            BendableBigDecimalScore divisor) {
        int dividendInitScore = dividend.initScore();
        int divisorInitScore = sanitize(divisor.initScore());
        BigDecimal[] hardScores = new BigDecimal[hardLevelsSize];
        for (int i = 0; i < hardLevelsSize; i++) {
            hardScores[i] = divide(dividend.hardScore(i), sanitize(divisor.hardScore(i)));
        }
        BigDecimal[] softScores = new BigDecimal[softLevelsSize];
        for (int i = 0; i < softLevelsSize; i++) {
            softScores[i] = divide(dividend.softScore(i), sanitize(divisor.softScore(i)));
        }
        BigDecimal[] levels = Stream.concat(Arrays.stream(hardScores), Arrays.stream(softScores))
                .toArray(BigDecimal[]::new);
        return createScoreUninitialized(divide(dividendInitScore, divisorInitScore), levels);
    }

    @Override
    public Class<?> getNumericType() {
        return BigDecimal.class;
    }
}
