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

package org.optaplanner.core.api.score.buildin.hardsoftlong;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HardSoftLongScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(HardSoftLongScore.parseScore("-147hard/-258soft"))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(-147L, -258L));
        assertThat(HardSoftLongScore.parseScore("-7init/-147hard/-258soft"))
                .isEqualTo(HardSoftLongScore.valueOf(-7, -147L, -258L));
    }

    @Test
    public void testToString() {
        assertThat(HardSoftLongScore.valueOfInitialized(-147L, -258L).toString()).isEqualTo("-147hard/-258soft");
        assertThat(HardSoftLongScore.valueOf(-7, -147L, -258L).toString()).isEqualTo("-7init/-147hard/-258soft");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardSoftLongScore.parseScore("-147");
    }

    @Test
    public void toInitializedScore() {
        assertThat(HardSoftLongScore.valueOfInitialized(-147L, -258L).toInitializedScore())
                .isEqualTo(HardSoftLongScore.valueOfInitialized(-147L, -258L));
        assertThat(HardSoftLongScore.valueOf(-7, -147L, -258L).toInitializedScore())
                .isEqualTo(HardSoftLongScore.valueOfInitialized(-147L, -258L));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardSoftLongScore.valueOfInitialized(-5L, -300L),
                HardSoftLongScore.valueOf(-7, -5L, -300L),
                HardSoftLongScore.valueOf(-7, 0L, -300L)
        );
        assertScoreFeasible(
                HardSoftLongScore.valueOfInitialized(0L, -300L),
                HardSoftLongScore.valueOfInitialized(2L, -300L),
                HardSoftLongScore.valueOf(0, 0L, -300L)
        );
    }

    @Test
    public void add() {
        assertThat(HardSoftLongScore.valueOfInitialized(20L, -20L).add(
                        HardSoftLongScore.valueOfInitialized(-1L, -300L)))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(19L, -320L));
        assertThat(HardSoftLongScore.valueOf(-70, 20L, -20L).add(
                        HardSoftLongScore.valueOf(-7, -1L, -300L)))
                .isEqualTo(HardSoftLongScore.valueOf(-77, 19L, -320L));
    }

    @Test
    public void subtract() {
        assertThat(HardSoftLongScore.valueOfInitialized(20L, -20L).subtract(
                        HardSoftLongScore.valueOfInitialized(-1L, -300L)))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(21L, 280L));
        assertThat(HardSoftLongScore.valueOf(-70, 20L, -20L).subtract(
                        HardSoftLongScore.valueOf(-7, -1L, -300L)))
                .isEqualTo(HardSoftLongScore.valueOf(-63, 21L, 280L));
    }

    @Test
    public void multiply() {
        assertThat(HardSoftLongScore.valueOfInitialized(5L, -5L).multiply(1.2))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(6L, -6L));
        assertThat(HardSoftLongScore.valueOfInitialized(1L, -1L).multiply(1.2))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(1L, -2L));
        assertThat(HardSoftLongScore.valueOfInitialized(4L, -4L).multiply(1.2))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(4L, -5L));
        assertThat(HardSoftLongScore.valueOf(-7, 4L, -5L).multiply(2.0))
                .isEqualTo(HardSoftLongScore.valueOf(-14, 8L, -10L));
    }

    @Test
    public void divide() {
        assertThat(HardSoftLongScore.valueOfInitialized(25L, -25L).divide(5.0))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(5L, -5L));
        assertThat(HardSoftLongScore.valueOfInitialized(21L, -21L).divide(5.0))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(4L, -5L));
        assertThat(HardSoftLongScore.valueOfInitialized(24L, -24L).divide(5.0))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(4L, -5L));
        assertThat(HardSoftLongScore.valueOf(-14, 8L, -10L).divide(2.0))
                .isEqualTo(HardSoftLongScore.valueOf(-7, 4L, -5L));
    }

    @Test
    public void power() {
        assertThat(HardSoftLongScore.valueOfInitialized(-4L, 5L).power(2.0))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(16L, 25L));
        assertThat(HardSoftLongScore.valueOfInitialized(16L, 25L).power(0.5))
                .isEqualTo(HardSoftLongScore.valueOfInitialized(4L, 5L));
        assertThat(HardSoftLongScore.valueOf(-7, -4L, 5L).power(3.0))
                .isEqualTo(HardSoftLongScore.valueOf(-343, -64L, 125L));
    }

    @Test
    public void negate() {
        assertThat(HardSoftLongScore.valueOfInitialized(4L, -5L).negate())
                .isEqualTo(HardSoftLongScore.valueOfInitialized(-4L, 5L));
        assertThat(HardSoftLongScore.valueOfInitialized(-4L, 5L).negate())
                .isEqualTo(HardSoftLongScore.valueOfInitialized(4L, -5L));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardSoftLongScore.valueOfInitialized(-10L, -200L),
                HardSoftLongScore.valueOfInitialized(-10L, -200L),
                HardSoftLongScore.valueOf(0, -10L, -200L)
        );
        assertScoresEqualsAndHashCode(
                HardSoftLongScore.valueOf(-7, -10L, -200L),
                HardSoftLongScore.valueOf(-7, -10L, -200L)
        );
        assertScoresNotEquals(
                HardSoftLongScore.valueOfInitialized(-10L, -200L),
                HardSoftLongScore.valueOfInitialized(-30L, -200L),
                HardSoftLongScore.valueOfInitialized(-10L, -400L),
                HardSoftLongScore.valueOf(-7, -10L, -200L)
        );
    }

    @Test
    public void compareTo() {
        assertThat(Arrays.asList(
                HardSoftLongScore.valueOf(-8, 0L, 0L),
                HardSoftLongScore.valueOf(-7, -20L, -20L),
                HardSoftLongScore.valueOf(-7, -1L, -300L),
                HardSoftLongScore.valueOf(-7, 0L, 0L),
                HardSoftLongScore.valueOf(-7, 0L, 1L),
                HardSoftLongScore.valueOfInitialized(-20L, Long.MIN_VALUE),
                HardSoftLongScore.valueOfInitialized(-20L, -20L),
                HardSoftLongScore.valueOfInitialized(-1L, -300L),
                HardSoftLongScore.valueOfInitialized(-1L, 4000L),
                HardSoftLongScore.valueOfInitialized(0L, -1L),
                HardSoftLongScore.valueOfInitialized(0L, 0L),
                HardSoftLongScore.valueOfInitialized(0L, 1L)
        )).isSorted();
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftLongScore.valueOfInitialized(-12, 3400L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore()).isEqualTo(-12L);
                    assertThat(output.getSoftScore()).isEqualTo(3400L);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftLongScore.valueOf(-7, -12L, 3400L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore()).isEqualTo(-12L);
                    assertThat(output.getSoftScore()).isEqualTo(3400L);
                }
        );
    }

}
