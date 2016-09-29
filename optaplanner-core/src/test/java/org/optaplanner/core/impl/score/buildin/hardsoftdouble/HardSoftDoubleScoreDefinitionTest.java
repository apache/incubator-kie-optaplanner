/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class HardSoftDoubleScoreDefinitionTest {

    @Test
    public void getLevelSize() {
        assertThat(new HardSoftDoubleScoreDefinition().getLevelsSize()).isEqualTo(2);
    }

    @Test
    public void getLevelLabels() {
        assertArrayEquals(new String[]{"hard score", "soft score"}, new HardSoftDoubleScoreDefinition().getLevelLabels());
    }

    @Test
    public void getFeasibleLevelsSize() {
        assertThat(new HardSoftDoubleScoreDefinition().getFeasibleLevelsSize()).isEqualTo(1);
    }

    @Test
    public void buildOptimisticBoundOnlyUp() {
        HardSoftDoubleScoreDefinition scoreDefinition = new HardSoftDoubleScoreDefinition();
        HardSoftDoubleScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftDoubleScore.valueOfInitialized(-1.7, -2.2));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(Double.POSITIVE_INFINITY, offset(0.0));
        assertThat(optimisticBound.getSoftScore()).isEqualTo(Double.POSITIVE_INFINITY, offset(0.0));
    }

    @Test
    public void buildOptimisticBoundOnlyDown() {
        HardSoftDoubleScoreDefinition scoreDefinition = new HardSoftDoubleScoreDefinition();
        HardSoftDoubleScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftDoubleScore.valueOfInitialized(-1.7, -2.2));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(-1.7, offset(0.0));
        assertThat(optimisticBound.getSoftScore()).isEqualTo(-2.2, offset(0.0));
    }

    @Test
    public void buildPessimisticBoundOnlyUp() {
        HardSoftDoubleScoreDefinition scoreDefinition = new HardSoftDoubleScoreDefinition();
        HardSoftDoubleScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftDoubleScore.valueOfInitialized(-1.7, -2.2));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(-1.7, offset(0.0));
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(-2.2, offset(0.0));
    }

    @Test
    public void buildPessimisticBoundOnlyDown() {
        HardSoftDoubleScoreDefinition scoreDefinition = new HardSoftDoubleScoreDefinition();
        HardSoftDoubleScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftDoubleScore.valueOfInitialized(-1, -2));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(Double.NEGATIVE_INFINITY, offset(0.0));
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(Double.NEGATIVE_INFINITY, offset(0.0));
    }

}
