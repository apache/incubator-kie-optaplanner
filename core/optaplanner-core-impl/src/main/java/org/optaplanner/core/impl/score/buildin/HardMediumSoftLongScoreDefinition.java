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

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.definition.AbstractScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class HardMediumSoftLongScoreDefinition extends AbstractScoreDefinition<HardMediumSoftLongScore> {

    public HardMediumSoftLongScoreDefinition() {
        super(new String[] { "hard score", "medium score", "soft score" });
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
        return 3;
    }

    @Override
    public int getFeasibleLevelsSize() {
        return 1;
    }

    @Override
    public Class<HardMediumSoftLongScore> getScoreClass() {
        return HardMediumSoftLongScore.class;
    }

    @Override
    public HardMediumSoftLongScore getZeroScore() {
        return HardMediumSoftLongScore.ZERO;
    }

    @Override
    public HardMediumSoftLongScore getOneSoftestScore() {
        return HardMediumSoftLongScore.ONE_SOFT;
    }

    @Override
    public HardMediumSoftLongScore parseScore(String scoreString) {
        return HardMediumSoftLongScore.parseScore(scoreString);
    }

    @Override
    public HardMediumSoftLongScore fromLevelNumbers(int initScore, Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        return HardMediumSoftLongScore.ofUninitialized(initScore, (Long) levelNumbers[0], (Long) levelNumbers[1],
                (Long) levelNumbers[2]);
    }

    @Override
    public HardMediumSoftLongScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend,
            HardMediumSoftLongScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
        return HardMediumSoftLongScore.ofUninitialized(0,
                trendLevels[0] == InitializingScoreTrendLevel.ONLY_DOWN ? score.hardScore() : Long.MAX_VALUE,
                trendLevels[1] == InitializingScoreTrendLevel.ONLY_DOWN ? score.mediumScore() : Long.MAX_VALUE,
                trendLevels[2] == InitializingScoreTrendLevel.ONLY_DOWN ? score.softScore() : Long.MAX_VALUE);
    }

    @Override
    public HardMediumSoftLongScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend,
            HardMediumSoftLongScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
        return HardMediumSoftLongScore.ofUninitialized(0,
                trendLevels[0] == InitializingScoreTrendLevel.ONLY_UP ? score.hardScore() : Long.MIN_VALUE,
                trendLevels[1] == InitializingScoreTrendLevel.ONLY_UP ? score.mediumScore() : Long.MIN_VALUE,
                trendLevels[2] == InitializingScoreTrendLevel.ONLY_UP ? score.softScore() : Long.MIN_VALUE);
    }

    @Override
    public HardMediumSoftLongScore divideBySanitizedDivisor(HardMediumSoftLongScore dividend,
            HardMediumSoftLongScore divisor) {
        int dividendInitScore = dividend.initScore();
        int divisorInitScore = sanitize(divisor.initScore());
        long dividendHardScore = dividend.hardScore();
        long divisorHardScore = sanitize(divisor.hardScore());
        long dividendMediumScore = dividend.mediumScore();
        long divisorMediumScore = sanitize(divisor.mediumScore());
        long dividendSoftScore = dividend.softScore();
        long divisorSoftScore = sanitize(divisor.softScore());
        return fromLevelNumbers(
                divide(dividendInitScore, divisorInitScore),
                new Number[] {
                        divide(dividendHardScore, divisorHardScore),
                        divide(dividendMediumScore, divisorMediumScore),
                        divide(dividendSoftScore, divisorSoftScore)
                });
    }

    @Override
    public Class<?> getNumericType() {
        return long.class;
    }
}
