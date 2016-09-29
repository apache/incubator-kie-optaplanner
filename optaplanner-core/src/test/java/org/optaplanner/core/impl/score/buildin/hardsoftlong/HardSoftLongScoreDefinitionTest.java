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

package org.optaplanner.core.impl.score.buildin.hardsoftlong;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

import static org.assertj.core.api.Assertions.assertThat;

public class HardSoftLongScoreDefinitionTest {

    @Test
    public void getLevelSize() {
        assertThat(new HardSoftLongScoreDefinition().getLevelsSize()).isEqualTo(2);
    }

    @Test
    public void getLevelLabels() {
        assertArrayEquals(new String[]{"hard score", "soft score"}, new HardSoftLongScoreDefinition().getLevelLabels());
    }

    @Test
    public void getFeasibleLevelsSize() {
        assertThat(new HardSoftLongScoreDefinition().getFeasibleLevelsSize()).isEqualTo(1);
    }

    @Test
    public void buildOptimisticBoundOnlyUp() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftLongScore.valueOfInitialized(-1L, -2L));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(Long.MAX_VALUE);
        assertThat(optimisticBound.getSoftScore()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    public void buildOptimisticBoundOnlyDown() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftLongScore.valueOfInitialized(-1L, -2L));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(-1L);
        assertThat(optimisticBound.getSoftScore()).isEqualTo(-2L);
    }

    @Test
    public void buildPessimisticBoundOnlyUp() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftLongScore.valueOfInitialized(-1L, -2L));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(-1L);
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(-2L);
    }

    @Test
    public void buildPessimisticBoundOnlyDown() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftLongScore.valueOfInitialized(-1L, -2L));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(Long.MIN_VALUE);
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(Long.MIN_VALUE);
    }

}
