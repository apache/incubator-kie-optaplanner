/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.buildin.hardsoftdouble;

import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScoreHolder;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.definition.AbstractFeasibilityScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

public class HardSoftDoubleScoreDefinition extends AbstractFeasibilityScoreDefinition<HardSoftDoubleScore> {

    public HardSoftDoubleScoreDefinition() {
        super(new String[]{"hard score", "soft score"});
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

    public Class<HardSoftDoubleScore> getScoreClass() {
        return HardSoftDoubleScore.class;
    }

    public HardSoftDoubleScore parseScore(String scoreString) {
        return HardSoftDoubleScore.parseScore(scoreString);
    }

    @Override
    public HardSoftDoubleScore fromLevelNumbers(Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        return HardSoftDoubleScore.valueOf((Double) levelNumbers[0], (Double) levelNumbers[1]);
    }

    public HardSoftDoubleScoreHolder buildScoreHolder(boolean constraintMatchEnabled) {
        return new HardSoftDoubleScoreHolder(constraintMatchEnabled);
    }

    public HardSoftDoubleScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend, HardSoftDoubleScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
        return HardSoftDoubleScore.valueOf(
                trendLevels[0] == InitializingScoreTrendLevel.ONLY_DOWN ? score.getHardScore() : Double.POSITIVE_INFINITY,
                trendLevels[1] == InitializingScoreTrendLevel.ONLY_DOWN ? score.getSoftScore() : Double.POSITIVE_INFINITY);
    }

    public HardSoftDoubleScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend, HardSoftDoubleScore score) {
        InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
        return HardSoftDoubleScore.valueOf(
                trendLevels[0] == InitializingScoreTrendLevel.ONLY_UP ? score.getHardScore() : Double.NEGATIVE_INFINITY,
                trendLevels[1] == InitializingScoreTrendLevel.ONLY_UP ? score.getSoftScore() : Double.NEGATIVE_INFINITY);
    }

}
