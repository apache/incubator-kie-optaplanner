/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.buildin.hardsoft;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.buildin.AbstractScoreDefinitionTest;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

import static org.assertj.core.api.Assertions.assertThat;

public class HardSoftScoreDefinitionTest extends AbstractScoreDefinitionTest {

    @Test
    public void getLevelsSize() {
        assertThat(new HardSoftScoreDefinition().getLevelsSize()).isEqualTo(2);
    }

    @Test
    public void getLevelLabels() {
        assertThat(new HardSoftScoreDefinition().getLevelLabels()).containsExactly("hard score", "soft score");
    }

    @Test
    public void getFeasibleLevelsSize() {
        assertThat(new HardSoftScoreDefinition().getFeasibleLevelsSize()).isEqualTo(1);
    }

    @Test
    public void buildOptimisticBoundOnlyUp() {
        HardSoftScoreDefinition scoreDefinition = new HardSoftScoreDefinition();
        HardSoftScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftScore.valueOfInitialized(-1, -2));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(Integer.MAX_VALUE);
        assertThat(optimisticBound.getSoftScore()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void buildOptimisticBoundOnlyDown() {
        HardSoftScoreDefinition scoreDefinition = new HardSoftScoreDefinition();
        HardSoftScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftScore.valueOfInitialized(-1, -2));
        assertThat(optimisticBound.getInitScore()).isEqualTo(0);
        assertThat(optimisticBound.getHardScore()).isEqualTo(-1);
        assertThat(optimisticBound.getSoftScore()).isEqualTo(-2);
    }

    @Test
    public void buildPessimisticBoundOnlyUp() {
        HardSoftScoreDefinition scoreDefinition = new HardSoftScoreDefinition();
        HardSoftScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 2),
                HardSoftScore.valueOfInitialized(-1, -2));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(-1);
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(-2);
    }

    @Test
    public void buildPessimisticBoundOnlyDown() {
        HardSoftScoreDefinition scoreDefinition = new HardSoftScoreDefinition();
        HardSoftScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 2),
                HardSoftScore.valueOfInitialized(-1, -2));
        assertThat(pessimisticBound.getInitScore()).isEqualTo(0);
        assertThat(pessimisticBound.getHardScore()).isEqualTo(Integer.MIN_VALUE);
        assertThat(pessimisticBound.getSoftScore()).isEqualTo(Integer.MIN_VALUE);
    }

}
