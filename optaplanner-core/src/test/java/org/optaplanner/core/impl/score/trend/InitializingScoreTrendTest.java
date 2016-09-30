/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.trend;

import org.junit.Test;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;

import static org.assertj.core.api.Assertions.assertThat;

public class InitializingScoreTrendTest {

    @Test
    public void parseTrend() {
        assertThat(InitializingScoreTrend.parseTrend("ONLY_DOWN/ANY/ONLY_UP", 3).getTrendLevels()).containsExactly(
                InitializingScoreTrendLevel.ONLY_DOWN,
                InitializingScoreTrendLevel.ANY,
                InitializingScoreTrendLevel.ONLY_UP);
    }

    @Test
    public void isOnlyUp() {
        assertThat(InitializingScoreTrend.parseTrend("ONLY_UP/ONLY_UP/ONLY_UP", 3).isOnlyUp()).isEqualTo(true);
        assertThat(InitializingScoreTrend.parseTrend("ONLY_UP/ANY/ONLY_UP", 3).isOnlyUp()).isEqualTo(false);
        assertThat(InitializingScoreTrend.parseTrend("ONLY_UP/ONLY_UP/ONLY_DOWN", 3).isOnlyUp()).isEqualTo(false);
    }

    @Test
    public void isOnlyDown() {
        assertThat(InitializingScoreTrend.parseTrend("ONLY_DOWN/ONLY_DOWN/ONLY_DOWN", 3).isOnlyDown()).isEqualTo(true);
        assertThat(InitializingScoreTrend.parseTrend("ONLY_DOWN/ANY/ONLY_DOWN", 3).isOnlyDown()).isEqualTo(false);
        assertThat(InitializingScoreTrend.parseTrend("ONLY_DOWN/ONLY_DOWN/ONLY_UP", 3).isOnlyDown()).isEqualTo(false);
    }

}
