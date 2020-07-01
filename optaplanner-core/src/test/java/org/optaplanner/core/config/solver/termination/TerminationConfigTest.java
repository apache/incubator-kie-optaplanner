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

package org.optaplanner.core.config.solver.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.solver.termination.Termination;
import org.optaplanner.core.impl.solver.termination.TimeMillisSpentTermination;
import org.optaplanner.core.impl.solver.termination.UnimprovedTimeMillisSpentTermination;

public class TerminationConfigTest {

    @Test
    public void spentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSpentLimit(Duration.ofMillis(5L)
                .plusSeconds(4L)
                .plusMinutes(3L)
                .plusHours(2L)
                .plusDays(1L));
        Termination termination = terminationConfig.buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(TimeMillisSpentTermination.class);
        assertThat(((TimeMillisSpentTermination) termination).getTimeMillisSpentLimit()).isEqualTo(93784005L);
    }

    @Test
    public void spentLimitWithoutJavaTime() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMillisecondsSpentLimit(5L);
        terminationConfig.setSecondsSpentLimit(4L);
        terminationConfig.setMinutesSpentLimit(3L);
        terminationConfig.setHoursSpentLimit(2L);
        terminationConfig.setDaysSpentLimit(1L);
        Termination termination = terminationConfig.buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(TimeMillisSpentTermination.class);
        assertThat(((TimeMillisSpentTermination) termination).getTimeMillisSpentLimit()).isEqualTo(93784005L);
    }

    @Test
    public void overwriteSpentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMinutesSpentLimit(1L);
        assertThat(terminationConfig.getMinutesSpentLimit()).isNotNull();
        terminationConfig.overwriteSpentLimit(Duration.ofHours(2L));
        assertThat(terminationConfig.getMinutesSpentLimit()).isNull();
    }

    @Test
    public void unimprovedSpentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setUnimprovedSpentLimit(Duration.ofMillis(5L)
                .plusSeconds(4L)
                .plusMinutes(3L)
                .plusHours(2L)
                .plusDays(1L));
        Termination termination = terminationConfig.buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(UnimprovedTimeMillisSpentTermination.class);
        assertThat(((UnimprovedTimeMillisSpentTermination) termination).getUnimprovedTimeMillisSpentLimit())
                .isEqualTo(93784005L);
    }

    @Test
    public void unimprovedSpentLimitWithoutJavaTime() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setUnimprovedMillisecondsSpentLimit(5L);
        terminationConfig.setUnimprovedSecondsSpentLimit(4L);
        terminationConfig.setUnimprovedMinutesSpentLimit(3L);
        terminationConfig.setUnimprovedHoursSpentLimit(2L);
        terminationConfig.setUnimprovedDaysSpentLimit(1L);
        Termination termination = terminationConfig.buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(UnimprovedTimeMillisSpentTermination.class);
        assertThat(((UnimprovedTimeMillisSpentTermination) termination).getUnimprovedTimeMillisSpentLimit())
                .isEqualTo(93784005L);
    }

    @Test
    public void overwriteUnimprovedSpentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setUnimprovedMinutesSpentLimit(1L);
        assertThat(terminationConfig.getUnimprovedMinutesSpentLimit()).isNotNull();
        terminationConfig.overwriteUnimprovedSpentLimit(Duration.ofHours(2L));
        assertThat(terminationConfig.getUnimprovedMinutesSpentLimit()).isNull();
    }

}
