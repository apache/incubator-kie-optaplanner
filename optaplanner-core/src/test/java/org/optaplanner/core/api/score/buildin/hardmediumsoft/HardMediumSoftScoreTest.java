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

package org.optaplanner.core.api.score.buildin.hardmediumsoft;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HardMediumSoftScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(HardMediumSoftScore.parseScore("-147hard/-258medium/-369soft"))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(-147, -258, -369));
        assertThat(HardMediumSoftScore.parseScore("-7init/-147hard/-258medium/-369soft"))
                .isEqualTo(HardMediumSoftScore.valueOf(-7, -147, -258, -369));
    }

    @Test
    public void testToString() {
        assertThat(HardMediumSoftScore.valueOfInitialized(-147, -258, -369).toString())
                .isEqualTo("-147hard/-258medium/-369soft");
        assertThat(HardMediumSoftScore.valueOf(-7, -147, -258, -369).toString())
                .isEqualTo("-7init/-147hard/-258medium/-369soft");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardMediumSoftScore.parseScore("-147");
    }

    @Test
    public void toInitializedScore() {
        assertThat(HardMediumSoftScore.valueOfInitialized(-147, -258, -369).toInitializedScore())
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(-147, -258, -369));
        assertThat(HardMediumSoftScore.valueOf(-7, -147, -258, -369).toInitializedScore())
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(-147, -258, -369));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardMediumSoftScore.valueOfInitialized(-5, -300, -4000),
                HardMediumSoftScore.valueOf(-7, -5, -300, -4000),
                HardMediumSoftScore.valueOf(-7, 0, -300, -4000)
        );
        assertScoreFeasible(
                HardMediumSoftScore.valueOfInitialized(0, -300, -4000),
                HardMediumSoftScore.valueOfInitialized(2, -300, -4000),
                HardMediumSoftScore.valueOf(0, 0, -300, -4000)
        );
    }

    @Test
    public void add() {
        assertThat(HardMediumSoftScore.valueOfInitialized(20, -20, -4000).add(
                        HardMediumSoftScore.valueOfInitialized(-1, -300, 4000)))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(19, -320, 0));
        assertThat(HardMediumSoftScore.valueOf(-70, 20, -20, -4000).add(
                        HardMediumSoftScore.valueOf(-7, -1, -300, 4000)))
                .isEqualTo(HardMediumSoftScore.valueOf(-77, 19, -320, 0));
    }

    @Test
    public void subtract() {
        assertThat(HardMediumSoftScore.valueOfInitialized(20, -20, -4000).subtract(
                        HardMediumSoftScore.valueOfInitialized(-1, -300, 4000)))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(21, 280, -8000));
        assertThat(HardMediumSoftScore.valueOf(-70, 20, -20, -4000).subtract(
                        HardMediumSoftScore.valueOf(-7, -1, -300, 4000)))
                .isEqualTo(HardMediumSoftScore.valueOf(-63, 21, 280, -8000));
    }

    @Test
    public void multiply() {
        assertThat(HardMediumSoftScore.valueOfInitialized(5, -5, 5).multiply(1.2))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(6, -6, 6));
        assertThat(HardMediumSoftScore.valueOfInitialized(1, -1, 1).multiply(1.2))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(1, -2, 1));
        assertThat(HardMediumSoftScore.valueOfInitialized(4, -4, 4).multiply(1.2))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(4, -5, 4));
        assertThat(HardMediumSoftScore.valueOf(-7, 4, -5, 6).multiply(2.0))
                .isEqualTo(HardMediumSoftScore.valueOf(-14, 8, -10, 12));
    }

    @Test
    public void divide() {
        assertThat(HardMediumSoftScore.valueOfInitialized(25, -25, 25).divide(5.0))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(5, -5, 5));
        assertThat(HardMediumSoftScore.valueOfInitialized(21, -21, 21).divide(5.0))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(4, -5, 4));
        assertThat(HardMediumSoftScore.valueOfInitialized(24, -24, 24).divide(5.0))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(4, -5, 4));
        assertThat(HardMediumSoftScore.valueOf(-14, 8, -10, 12).divide(2.0))
                .isEqualTo(HardMediumSoftScore.valueOf(-7, 4, -5, 6));
    }

    @Test
    public void power() {
        assertThat(HardMediumSoftScore.valueOfInitialized(3, -4, 5).power(2.0))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(9, 16, 25));
        assertThat(HardMediumSoftScore.valueOfInitialized(9, 16, 25).power(0.5))
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(3, 4, 5));
        assertThat(HardMediumSoftScore.valueOf(-7, 3, -4, 5).power(3.0))
                .isEqualTo(HardMediumSoftScore.valueOf(-343, 27, -64, 125));
    }

    @Test
    public void negate() {
        assertThat(HardMediumSoftScore.valueOfInitialized(3, -4, 5).negate())
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(-3, 4, -5));
        assertThat(HardMediumSoftScore.valueOfInitialized(-3, 4, -5).negate())
                .isEqualTo(HardMediumSoftScore.valueOfInitialized(3, -4, 5));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardMediumSoftScore.valueOfInitialized(-10, -200, -3000),
                HardMediumSoftScore.valueOfInitialized(-10, -200, -3000),
                HardMediumSoftScore.valueOf(0, -10, -200, -3000)
        );
        assertScoresEqualsAndHashCode(
                HardMediumSoftScore.valueOf(-7, -10, -200, -3000),
                HardMediumSoftScore.valueOf(-7, -10, -200, -3000)
        );
        assertScoresNotEquals(
                HardMediumSoftScore.valueOfInitialized(-10, -200, -3000),
                HardMediumSoftScore.valueOfInitialized(-30, -200, -3000),
                HardMediumSoftScore.valueOfInitialized(-10, -400, -3000),
                HardMediumSoftScore.valueOfInitialized(-10, -400, -5000),
                HardMediumSoftScore.valueOf(-7, -10, -200, -3000)
        );
    }

    @Test
    public void compareTo() {
        assertThat(Arrays.asList(
                HardMediumSoftScore.valueOf(-8, 0, 0, 0),
                HardMediumSoftScore.valueOf(-7, -20, -20, -20),
                HardMediumSoftScore.valueOf(-7, -1, -300, -4000),
                HardMediumSoftScore.valueOf(-7, 0, 0, 0),
                HardMediumSoftScore.valueOf(-7, 0, 0, 1),
                HardMediumSoftScore.valueOf(-7, 0, 1, 0),
                HardMediumSoftScore.valueOfInitialized(-20, Integer.MIN_VALUE, Integer.MIN_VALUE),
                HardMediumSoftScore.valueOfInitialized(-20, Integer.MIN_VALUE, -20),
                HardMediumSoftScore.valueOfInitialized(-20, Integer.MIN_VALUE, 1),
                HardMediumSoftScore.valueOfInitialized(-20, -300, -4000),
                HardMediumSoftScore.valueOfInitialized(-20, -300, -300),
                HardMediumSoftScore.valueOfInitialized(-20, -300, -20),
                HardMediumSoftScore.valueOfInitialized(-20, -300, 300),
                HardMediumSoftScore.valueOfInitialized(-20, -20, -300),
                HardMediumSoftScore.valueOfInitialized(-20, -20, 0),
                HardMediumSoftScore.valueOfInitialized(-20, -20, 1),
                HardMediumSoftScore.valueOfInitialized(-1, -300, -4000),
                HardMediumSoftScore.valueOfInitialized(-1, -300, -20),
                HardMediumSoftScore.valueOfInitialized(-1, -20, -300),
                HardMediumSoftScore.valueOfInitialized(1, Integer.MIN_VALUE, -20),
                HardMediumSoftScore.valueOfInitialized(1, -20, Integer.MIN_VALUE)
        )).isSorted();
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardMediumSoftScore.valueOfInitialized(-12, 3400, -56),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore()).isEqualTo(-12);
                    assertThat(output.getMediumScore()).isEqualTo(3400);
                    assertThat(output.getSoftScore()).isEqualTo(-56);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardMediumSoftScore.valueOf(-7, -12, 3400, -56),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore()).isEqualTo(-12);
                    assertThat(output.getMediumScore()).isEqualTo(3400);
                    assertThat(output.getSoftScore()).isEqualTo(-56);
                }
        );
    }

}
