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

package org.optaplanner.core.api.score.buildin.hardmediumsoftlong;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HardMediumSoftLongScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(HardMediumSoftLongScore.parseScore("-147hard/-258medium/-369soft"))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(-147L, -258L, -369L));
        assertThat(HardMediumSoftLongScore.parseScore("-7init/-147hard/-258medium/-369soft"))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-7, -147L, -258L, -369L));
    }

    @Test
    public void testToString() {
        assertEquals("-147hard/-258medium/-369soft",
                HardMediumSoftLongScore.valueOfInitialized(-147L, -258L, -369L).toString());
        assertEquals("-7init/-147hard/-258medium/-369soft",
                HardMediumSoftLongScore.valueOf(-7, -147L, -258L, -369L).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardMediumSoftLongScore.parseScore("-147");
    }

    @Test
    public void toInitializedScore() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(-147L, -258L, -369L).toInitializedScore())
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(-147L, -258L, -369L));
        assertThat(HardMediumSoftLongScore.valueOf(-7, -147L, -258L, -369L).toInitializedScore())
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(-147L, -258L, -369L));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardMediumSoftLongScore.valueOfInitialized(-5L, -300L, -4000L),
                HardMediumSoftLongScore.valueOf(-7, -5L, -300L, -4000L),
                HardMediumSoftLongScore.valueOf(-7, 0L, -300L, -4000L)
        );
        assertScoreFeasible(
                HardMediumSoftLongScore.valueOfInitialized(0L, -300L, -4000L),
                HardMediumSoftLongScore.valueOfInitialized(2L, -300L, -4000L),
                HardMediumSoftLongScore.valueOf(0, 0L, -300L, -4000L)
        );
    }

    @Test
    public void add() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(20L, -20L, -4000L).add(
                        HardMediumSoftLongScore.valueOfInitialized(-1L, -300L, 4000L)))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(19L, -320L, 0L));
        assertThat(HardMediumSoftLongScore.valueOf(-70, 20L, -20L, -4000L).add(
                        HardMediumSoftLongScore.valueOf(-7, -1L, -300L, 4000L)))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-77, 19L, -320L, 0L));
    }

    @Test
    public void subtract() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(20L, -20L, -4000L).subtract(
                        HardMediumSoftLongScore.valueOfInitialized(-1L, -300L, 4000L)))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(21L, 280L, -8000L));
        assertThat(HardMediumSoftLongScore.valueOf(-70, 20L, -20L, -4000L).subtract(
                        HardMediumSoftLongScore.valueOf(-7, -1L, -300L, 4000L)))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-63, 21L, 280L, -8000L));
    }

    @Test
    public void multiply() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(5L, -5L, 5L).multiply(1.2))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(6L, -6L, 6L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(1L, -1L, 1L).multiply(1.2))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(1L, -2L, 1L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(4L, -4L, 4L).multiply(1.2))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(4L, -5L, 4L));
        assertThat(HardMediumSoftLongScore.valueOf(-7, 4L, -5L, 6L).multiply(2.0))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-14, 8L, -10L, 12L));
    }

    @Test
    public void divide() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(25L, -25L, 25L).divide(5.0))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(5L, -5L, 5L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(21L, -21L, 21L).divide(5.0))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(4L, -5L, 4L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(24L, -24L, 24L).divide(5.0))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(4L, -5L, 4L));
        assertThat(HardMediumSoftLongScore.valueOf(-14, 8L, -10L, 12L).divide(2.0))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-7, 4L, -5L, 6L));
    }

    @Test
    public void power() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(3L, -4L, 5L).power(2.0))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(9L, 16L, 25L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(9L, 16L, 25L).power(0.5))
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(3L, 4L, 5L));
        assertThat(HardMediumSoftLongScore.valueOf(-7, 3L, -4L, 5L).power(3.0))
                .isEqualTo(HardMediumSoftLongScore.valueOf(-343, 27L, -64L, 125L));
    }

    @Test
    public void negate() {
        assertThat(HardMediumSoftLongScore.valueOfInitialized(3L, -4L, 5L).negate())
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(-3L, 4L, -5L));
        assertThat(HardMediumSoftLongScore.valueOfInitialized(-3L, 4L, -5L).negate())
                .isEqualTo(HardMediumSoftLongScore.valueOfInitialized(3L, -4L, 5L));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardMediumSoftLongScore.valueOfInitialized(-10L, -200L, -3000L),
                HardMediumSoftLongScore.valueOfInitialized(-10L, -200L, -3000L),
                HardMediumSoftLongScore.valueOf(0, -10L, -200L, -3000L)
        );
        assertScoresEqualsAndHashCode(
                HardMediumSoftLongScore.valueOf(-7, -10L, -200L, -3000L),
                HardMediumSoftLongScore.valueOf(-7, -10L, -200L, -3000L)
        );
        assertScoresNotEquals(
                HardMediumSoftLongScore.valueOfInitialized(-10L, -200L, -3000L),
                HardMediumSoftLongScore.valueOfInitialized(-30L, -200L, -3000L),
                HardMediumSoftLongScore.valueOfInitialized(-10L, -400L, -3000L),
                HardMediumSoftLongScore.valueOfInitialized(-10L, -400L, -5000L),
                HardMediumSoftLongScore.valueOf(-7, -10L, -200L, -3000L)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                HardMediumSoftLongScore.valueOf(-8, 0L, 0L, 0L),
                HardMediumSoftLongScore.valueOf(-7, -20L, -20L, -20L),
                HardMediumSoftLongScore.valueOf(-7, -1L, -300L, -4000L),
                HardMediumSoftLongScore.valueOf(-7, 0L, 0L, 0L),
                HardMediumSoftLongScore.valueOf(-7, 0L, 0L, 1L),
                HardMediumSoftLongScore.valueOf(-7, 0L, 1L, 0L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, Long.MIN_VALUE, Long.MIN_VALUE),
                HardMediumSoftLongScore.valueOfInitialized(-20L, Long.MIN_VALUE, -20L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, Long.MIN_VALUE, 1L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -300L, -4000L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -300L, -300L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -300L, -20L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -300L, 300L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -20L, -300L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -20L, 0L),
                HardMediumSoftLongScore.valueOfInitialized(-20L, -20L, 1L),
                HardMediumSoftLongScore.valueOfInitialized(-1L, -300L, -4000L),
                HardMediumSoftLongScore.valueOfInitialized(-1L, -300L, -20L),
                HardMediumSoftLongScore.valueOfInitialized(-1L, -20L, -300L),
                HardMediumSoftLongScore.valueOfInitialized(1L, Long.MIN_VALUE, -20L),
                HardMediumSoftLongScore.valueOfInitialized(1L, -20L, Long.MIN_VALUE)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardMediumSoftLongScore.valueOfInitialized(-12L, 3400L, -56L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore()).isEqualTo(-12L);
                    assertThat(output.getMediumScore()).isEqualTo(3400L);
                    assertThat(output.getSoftScore()).isEqualTo(-56L);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardMediumSoftLongScore.valueOf(-7, -12L, 3400L, -56L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore()).isEqualTo(-12L);
                    assertThat(output.getMediumScore()).isEqualTo(3400L);
                    assertThat(output.getSoftScore()).isEqualTo(-56L);
                }
        );
    }

}
