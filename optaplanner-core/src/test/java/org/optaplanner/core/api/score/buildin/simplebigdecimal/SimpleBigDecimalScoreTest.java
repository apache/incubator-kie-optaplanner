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

package org.optaplanner.core.api.score.buildin.simplebigdecimal;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleBigDecimalScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(SimpleBigDecimalScore.parseScore("-147.2"))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2")));
        assertThat(SimpleBigDecimalScore.parseScore("-7init/-147.2"))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-147.2")));
    }

    @Test
    public void testToString() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2"))).hasToString("-147.2");
        assertThat(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-147.2"))).hasToString("-7init/-147.2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        SimpleBigDecimalScore.parseScore("-147.2hard/-258.3soft");
    }

    @Test
    public void toInitializedScore() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2")).toInitializedScore())
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2")));
        assertThat(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-147.2")).toInitializedScore())
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-147.2")));
    }

    @Test
    public void add() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("20")).add(
                        SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-1"))))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("19")));
        assertThat(SimpleBigDecimalScore.valueOf(-70, new BigDecimal("20")).add(
                        SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-1"))))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-77, new BigDecimal("19")));
    }

    @Test
    public void subtract() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("20")).subtract(
                        SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-1"))))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("21")));
        assertThat(SimpleBigDecimalScore.valueOf(-70, new BigDecimal("20")).subtract(
                        SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-1"))))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-63, new BigDecimal("21")));
    }

    @Test
    public void multiply() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("5.0")).multiply(1.2))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("6.0")));
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("1.0")).multiply(1.2))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("1.2")));
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("4.0")).multiply(1.2))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("4.8")));
        assertThat(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("4.3")).multiply(2.0))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-14, new BigDecimal("8.6")));
    }

    @Test
    public void divide() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("25.0")).divide(5.0))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("5.0")));
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("21.0")).divide(5.0))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("4.2")));
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("24.0")).divide(5.0))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("4.8")));
        assertThat(SimpleBigDecimalScore.valueOf(-14, new BigDecimal("8.6")).divide(2.0))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("4.3")));
    }

    @Test
    public void power() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("5.0")).power(2.0))
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("25.0")));
        assertThat(SimpleBigDecimalScore.valueOf(-7, new BigDecimal("5.0")).power(3.0))
                .isEqualTo(SimpleBigDecimalScore.valueOf(-343, new BigDecimal("125.0")));
    }

    @Test
    public void negate() {
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("5.0")).negate())
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-5.0")));
        assertThat(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-5.0")).negate())
                .isEqualTo(SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("5.0")));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0")),
                SimpleBigDecimalScore.valueOf(0, new BigDecimal("-10.0"))
        );
        assertScoresEqualsAndHashCode(
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-10.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-10.0"))
        );
        assertScoresNotEquals(
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-10.0")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-30.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-10.0"))
        );
    }

    @Test
    public void compareTo() {
        assertThat(Arrays.asList(
                SimpleBigDecimalScore.valueOf(-8, new BigDecimal("0.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-20.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("-1.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("0.0")),
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("1.0")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-300.5")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-300")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-20.067")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-20.007")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-20")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("-1")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("0")),
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("1"))
        )).isSorted();
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleBigDecimalScore.valueOfInitialized(new BigDecimal("123.4")),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getScore()).isEqualTo(new BigDecimal("123.4"));
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleBigDecimalScore.valueOf(-7, new BigDecimal("123.4")),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getScore()).isEqualTo(new BigDecimal("123.4"));
                }
        );
    }

}
