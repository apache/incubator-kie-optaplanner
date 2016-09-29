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

package org.optaplanner.core.api.score.buildin.hardsoftdouble;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HardSoftDoubleScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(HardSoftDoubleScore.parseScore("-147.2hard/-258.3soft"))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(-147.2, -258.3));
        assertThat(HardSoftDoubleScore.parseScore("-7init/-147.2hard/-258.3soft"))
                .isEqualTo(HardSoftDoubleScore.valueOf(-7, -147.2, -258.3));
    }

    @Test
    public void testToString() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(-147.2, -258.3).toString()).isEqualTo("-147.2hard/-258.3soft");
        assertThat(HardSoftDoubleScore.valueOf(-7, -147.2, -258.3).toString()).isEqualTo("-7init/-147.2hard/-258.3soft");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardSoftDoubleScore.parseScore("-147.2");
    }

    @Test
    public void toInitializedScore() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(-147.2, -258.3).toInitializedScore())
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(-147.2, -258.3));
        assertThat(HardSoftDoubleScore.valueOf(-7, -147.2, -258.3).toInitializedScore())
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(-147.2, -258.3));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardSoftDoubleScore.valueOfInitialized(-5.0, -300.0),
                HardSoftDoubleScore.valueOfInitialized(-5.0, 4000.0),
                HardSoftDoubleScore.valueOfInitialized(-0.007, 4000.0),
                HardSoftDoubleScore.valueOf(-7, -5.0, -300.0),
                HardSoftDoubleScore.valueOf(-7, 0.0, -300.0)
        );
        assertScoreFeasible(
                HardSoftDoubleScore.valueOfInitialized(0.0, -300.007),
                HardSoftDoubleScore.valueOfInitialized(0.0, -300.0),
                HardSoftDoubleScore.valueOfInitialized(2.0, -300.0),
                HardSoftDoubleScore.valueOf(0, 0.0, -300.0)
        );
    }

    @Test
    public void add() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(20.0, -20.0).add(
                        HardSoftDoubleScore.valueOfInitialized(-1.0, -300.0)))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(19.0, -320.0));
        assertThat(HardSoftDoubleScore.valueOf(-70, 20.0, -20.0).add(
                        HardSoftDoubleScore.valueOf(-7, -1.0, -300.0)))
                .isEqualTo(HardSoftDoubleScore.valueOf(-77, 19.0, -320.0));
    }

    @Test
    public void subtract() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(20.0, -20.0).subtract(
                        HardSoftDoubleScore.valueOfInitialized(-1.0, -300.0)))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(21.0, 280.0));
        assertThat(HardSoftDoubleScore.valueOf(-70, 20.0, -20.0).subtract(
                        HardSoftDoubleScore.valueOf(-7, -1.0, -300.0)))
                .isEqualTo(HardSoftDoubleScore.valueOf(-63, 21.0, 280.0));
    }

    @Test
    public void multiply() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(5.0, -5.0).multiply(1.2))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(6.0, -6.0));
        assertThat(HardSoftDoubleScore.valueOfInitialized(1.0, -1.0).multiply(1.2))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(1.2, -1.2));
        assertThat(HardSoftDoubleScore.valueOfInitialized(4.0, -4.0).multiply(1.2))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(4.8, -4.8));
        assertThat(HardSoftDoubleScore.valueOf(-7, 4.3, -5.2).multiply(2.0))
                .isEqualTo(HardSoftDoubleScore.valueOf(-14, 8.6, -10.4));
    }

    @Test
    public void divide() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(25.0, -25.0).divide(5.0))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(5.0, -5.0));
        assertThat(HardSoftDoubleScore.valueOfInitialized(21.0, -21.0).divide(5.0))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(4.2, -4.2));
        assertThat(HardSoftDoubleScore.valueOfInitialized(24.0, -24.0).divide(5.0))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(4.8, -4.8));
        assertThat(HardSoftDoubleScore.valueOf(-14, 8.6, -10.4).divide(2.0))
                .isEqualTo(HardSoftDoubleScore.valueOf(-7, 4.3, -5.2));
    }

    @Test
    public void power() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(-4.0, 1.5).power(2.0))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(16.0, 2.25));
        assertThat(HardSoftDoubleScore.valueOfInitialized(16.0, 2.25).power(0.5))
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(4.0, 1.5));
        assertThat(HardSoftDoubleScore.valueOf(-7, -4.0, 5.0).power(3.0))
                .isEqualTo(HardSoftDoubleScore.valueOf(-343, -64.0, 125.0));
    }

    @Test
    public void negate() {
        assertThat(HardSoftDoubleScore.valueOfInitialized(4.0, -1.5).negate())
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(-4.0, 1.5));
        assertThat(HardSoftDoubleScore.valueOfInitialized(-4.0, 1.5).negate())
                .isEqualTo(HardSoftDoubleScore.valueOfInitialized(4.0, -1.5));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardSoftDoubleScore.valueOfInitialized(-10.0, -200.0),
                HardSoftDoubleScore.valueOfInitialized(-10.0, -200.0),
                HardSoftDoubleScore.valueOf(0, -10.0, -200.0)
        );
        assertScoresEqualsAndHashCode(
                HardSoftDoubleScore.valueOf(-7, -10.0, -200.0),
                HardSoftDoubleScore.valueOf(-7, -10.0, -200.0)
        );
        assertScoresNotEquals(
                HardSoftDoubleScore.valueOfInitialized(-10.0, -200.0),
                HardSoftDoubleScore.valueOfInitialized(-30.0, -200.0),
                HardSoftDoubleScore.valueOfInitialized(-10.0, -400.0),
                HardSoftDoubleScore.valueOf(-7, -10.0, -200.0)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                HardSoftDoubleScore.valueOf(-8, 0.0, 0.0),
                HardSoftDoubleScore.valueOf(-7, -20.0, -20.0),
                HardSoftDoubleScore.valueOf(-7, -1.0, -300.0),
                HardSoftDoubleScore.valueOf(-7, 0.0, 0.0),
                HardSoftDoubleScore.valueOf(-7, 0.0, 1.0),
                HardSoftDoubleScore.valueOfInitialized(-20.06, -20.0),
                HardSoftDoubleScore.valueOfInitialized(-20.007, -20.0),
                HardSoftDoubleScore.valueOfInitialized(-20.0, -Double.MAX_VALUE),
                HardSoftDoubleScore.valueOfInitialized(-20.0, -20.06),
                HardSoftDoubleScore.valueOfInitialized(-20.0, -20.007),
                HardSoftDoubleScore.valueOfInitialized(-20.0, -20.0),
                HardSoftDoubleScore.valueOfInitialized(-1.0, -300.0),
                HardSoftDoubleScore.valueOfInitialized(-1.0, 4000.0),
                HardSoftDoubleScore.valueOfInitialized(0.0, -1.0),
                HardSoftDoubleScore.valueOfInitialized(0.0, 0.0),
                HardSoftDoubleScore.valueOfInitialized(0.0, 1.0)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftDoubleScore.valueOfInitialized(-12.3, 3400.5),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertEquals(-12.3, output.getHardScore(), 0.0);
                    assertEquals(3400.5, output.getSoftScore(), 0.0);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftDoubleScore.valueOf(-7, -12.3, 3400.5),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertEquals(-12.3, output.getHardScore(), 0.0);
                    assertEquals(3400.5, output.getSoftScore(), 0.0);
                }
        );
    }

}
