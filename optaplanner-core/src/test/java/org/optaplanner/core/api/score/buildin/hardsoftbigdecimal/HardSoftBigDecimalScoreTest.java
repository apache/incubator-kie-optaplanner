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

package org.optaplanner.core.api.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HardSoftBigDecimalScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(HardSoftBigDecimalScore.parseScore("-147.2hard/-258.3soft"))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"), new BigDecimal("-258.3")));
        assertThat(HardSoftBigDecimalScore.parseScore("-7init/-147.2hard/-258.3soft"))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-147.2"), new BigDecimal("-258.3")));
    }

    @Test
    public void testToString() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"), new BigDecimal("-258.3")).toString())
                .isEqualTo("-147.2hard/-258.3soft");
        assertThat(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-147.2"), new BigDecimal("-258.3")).toString())
                .isEqualTo("-7init/-147.2hard/-258.3soft");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardSoftBigDecimalScore.parseScore("-147.2");
    }

    @Test
    public void toInitializedScore() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"), new BigDecimal("-258.3")).toInitializedScore())
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"), new BigDecimal("-258.3")));
        assertThat(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-147.2"), new BigDecimal("-258.3")).toInitializedScore())
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"), new BigDecimal("-258.3")));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-5"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-5"), new BigDecimal("4000")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-0.007"), new BigDecimal("4000")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-5"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("0"), new BigDecimal("-300"))
        );
        assertScoreFeasible(
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("0"), new BigDecimal("-300.007")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("0"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("2"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOf(0, new BigDecimal("0"), new BigDecimal("-300"))
        );
    }

    @Test
    public void add() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("20"), new BigDecimal("-20")).add(
                        HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-1"), new BigDecimal("-300"))))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("19"), new BigDecimal("-320")));
        assertThat(HardSoftBigDecimalScore.valueOf(-70, new BigDecimal("20"), new BigDecimal("-20")).add(
                        HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-1"), new BigDecimal("-300"))))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-77, new BigDecimal("19"), new BigDecimal("-320")));
    }

    @Test
    public void subtract() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("20"), new BigDecimal("-20")).subtract(
                        HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-1"), new BigDecimal("-300"))))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("21"), new BigDecimal("280")));
        assertThat(HardSoftBigDecimalScore.valueOf(-70, new BigDecimal("20"), new BigDecimal("-20")).subtract(
                        HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-1"), new BigDecimal("-300"))))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-63, new BigDecimal("21"), new BigDecimal("280")));
    }

    @Test
    public void multiply() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("5.0"), new BigDecimal("-5.0")).multiply(1.2))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("6.0"), new BigDecimal("-6.0")));
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("1.0"), new BigDecimal("-1.0")).multiply(1.2))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("1.2"), new BigDecimal("-1.2")));
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.0"), new BigDecimal("-4.0")).multiply(1.2))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.8"), new BigDecimal("-4.8")));
        assertThat(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("4.3"), new BigDecimal("-5.2")).multiply(2.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-14, new BigDecimal("8.6"), new BigDecimal("-10.4")));
    }

    @Test
    public void divide() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("25.0"), new BigDecimal("-25.0")).divide(5.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("5.0"), new BigDecimal("-5.0")));
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("21.0"), new BigDecimal("-21.0")).divide(5.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.2"), new BigDecimal("-4.2")));
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("24.0"), new BigDecimal("-24.0")).divide(5.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.8"), new BigDecimal("-4.8")));
        assertThat(HardSoftBigDecimalScore.valueOf(-14, new BigDecimal("8.6"), new BigDecimal("-10.4")).divide(2.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("4.3"), new BigDecimal("-5.2")));
    }

    @Test
    public void power() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-4.0"), new BigDecimal("5.0")).power(2.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("16.0"), new BigDecimal("25.0")));
        assertThat(HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-4.0"), new BigDecimal("5.0")).power(3.0))
                .isEqualTo(HardSoftBigDecimalScore.valueOf(-343, new BigDecimal("-64.0"), new BigDecimal("125.0")));
    }

    @Test
    public void negate() {
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.0"), new BigDecimal("-5.0")).negate())
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-4.0"), new BigDecimal("5.0")));
        assertThat(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-4.0"), new BigDecimal("5.0")).negate())
                .isEqualTo(HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("4.0"), new BigDecimal("-5.0")));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0"), new BigDecimal("-200.0")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0"), new BigDecimal("-200.0")),
                HardSoftBigDecimalScore.valueOf(0, new BigDecimal("-10.0"), new BigDecimal("-200.0"))
        );
        assertScoresEqualsAndHashCode(
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-10.0"), new BigDecimal("-200.0")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-10.0"), new BigDecimal("-200.0"))
        );
        assertScoresNotEquals(
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0"), new BigDecimal("-200.0")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-30.0"), new BigDecimal("-200.0")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0"), new BigDecimal("-400.0")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-10.0"), new BigDecimal("-200.0"))
        );
    }

    @Test
    public void compareTo() {
        assertThat(Arrays.asList(
                HardSoftBigDecimalScore.valueOf(-8, new BigDecimal("0"), new BigDecimal("0")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-20"), new BigDecimal("-20")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-1"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("0"), new BigDecimal("0")),
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("0"), new BigDecimal("1")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-20.06"), new BigDecimal("-20")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-20.007"), new BigDecimal("-20")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-20"), new BigDecimal("-20.06")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-20"), new BigDecimal("-20.007")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-20"), new BigDecimal("-20")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-1"), new BigDecimal("-300")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-1"), new BigDecimal("4000")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("0"), new BigDecimal("-1")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("0"), new BigDecimal("0")),
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("0"), new BigDecimal("1"))
        )).isSorted();
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftBigDecimalScore.valueOfInitialized(new BigDecimal("-12.3"), new BigDecimal("3400.5")),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore()).isEqualTo(new BigDecimal("-12.3"));
                    assertThat(output.getSoftScore()).isEqualTo(new BigDecimal("3400.5"));
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftBigDecimalScore.valueOf(-7, new BigDecimal("-12.3"), new BigDecimal("3400.5")),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore()).isEqualTo(new BigDecimal("-12.3"));
                    assertThat(output.getSoftScore()).isEqualTo(new BigDecimal("3400.5"));
                }
        );
    }

}
