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

package org.optaplanner.core.api.score.buildin.simple;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(SimpleScore.parseScore("-147")).isEqualTo(SimpleScore.valueOfInitialized(-147));
        assertEquals(SimpleScore.valueOf(-7, -147), SimpleScore.parseScore("-7init/-147"));
    }

    @Test
    public void testToString() {
        assertThat(SimpleScore.valueOfInitialized(-147).toString()).isEqualTo("-147");
        assertEquals("-7init/-147", SimpleScore.valueOf(-7, -147).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        SimpleScore.parseScore("-147hard/-258soft");
    }

    @Test
    public void toInitializedScore() {
        assertThat(SimpleScore.valueOfInitialized(-147).toInitializedScore())
                .isEqualTo(SimpleScore.valueOfInitialized(-147));
        assertThat(SimpleScore.valueOf(-7, -147).toInitializedScore())
                .isEqualTo(SimpleScore.valueOfInitialized(-147));
    }

    @Test
    public void add() {
        assertThat(SimpleScore.valueOfInitialized(20).add(
                        SimpleScore.valueOfInitialized(-1)))
                .isEqualTo(SimpleScore.valueOfInitialized(19));
        assertThat(SimpleScore.valueOf(-70, 20).add(
                        SimpleScore.valueOf(-7, -1)))
                .isEqualTo(SimpleScore.valueOf(-77, 19));
    }

    @Test
    public void subtract() {
        assertThat(SimpleScore.valueOfInitialized(20).subtract(
                        SimpleScore.valueOfInitialized(-1)))
                .isEqualTo(SimpleScore.valueOfInitialized(21));
        assertThat(SimpleScore.valueOf(-70, 20).subtract(
                        SimpleScore.valueOf(-7, -1)))
                .isEqualTo(SimpleScore.valueOf(-63, 21));
    }

    @Test
    public void multiply() {
        assertThat(SimpleScore.valueOfInitialized(5).multiply(1.2))
                .isEqualTo(SimpleScore.valueOfInitialized(6));
        assertThat(SimpleScore.valueOfInitialized(1).multiply(1.2))
                .isEqualTo(SimpleScore.valueOfInitialized(1));
        assertThat(SimpleScore.valueOfInitialized(4).multiply(1.2))
                .isEqualTo(SimpleScore.valueOfInitialized(4));
        assertThat(SimpleScore.valueOf(-7, 4).multiply(2.0))
                .isEqualTo(SimpleScore.valueOf(-14, 8));
    }

    @Test
    public void divide() {
        assertThat(SimpleScore.valueOfInitialized(25).divide(5.0))
                .isEqualTo(SimpleScore.valueOfInitialized(5));
        assertThat(SimpleScore.valueOfInitialized(21).divide(5.0))
                .isEqualTo(SimpleScore.valueOfInitialized(4));
        assertThat(SimpleScore.valueOfInitialized(24).divide(5.0))
                .isEqualTo(SimpleScore.valueOfInitialized(4));
        assertThat(SimpleScore.valueOf(-14, 8).divide(2.0))
                .isEqualTo(SimpleScore.valueOf(-7, 4));
    }

    @Test
    public void power() {
        assertThat(SimpleScore.valueOfInitialized(5).power(2.0))
                .isEqualTo(SimpleScore.valueOfInitialized(25));
        assertThat(SimpleScore.valueOfInitialized(25).power(0.5))
                .isEqualTo(SimpleScore.valueOfInitialized(5));
        assertThat(SimpleScore.valueOf(-7, 5).power(3.0))
                .isEqualTo(SimpleScore.valueOf(-343, 125));
    }

    @Test
    public void negate() {
        assertThat(SimpleScore.valueOfInitialized(5).negate())
                .isEqualTo(SimpleScore.valueOfInitialized(-5));
        assertThat(SimpleScore.valueOfInitialized(-5).negate())
                .isEqualTo(SimpleScore.valueOfInitialized(5));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                SimpleScore.valueOfInitialized(-10),
                SimpleScore.valueOfInitialized(-10),
                SimpleScore.valueOf(0, -10)
        );
        assertScoresEqualsAndHashCode(
                SimpleScore.valueOf(-7, -10),
                SimpleScore.valueOf(-7, -10)
        );
        assertScoresNotEquals(
                SimpleScore.valueOfInitialized(-10),
                SimpleScore.valueOfInitialized(-30),
                SimpleScore.valueOf(-7, -10)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                SimpleScore.valueOf(-8, 0),
                SimpleScore.valueOf(-7, -20),
                SimpleScore.valueOf(-7, -1),
                SimpleScore.valueOf(-7, 0),
                SimpleScore.valueOf(-7, 1),
                SimpleScore.valueOfInitialized(-300),
                SimpleScore.valueOfInitialized(-20),
                SimpleScore.valueOfInitialized(-1),
                SimpleScore.valueOfInitialized(0),
                SimpleScore.valueOfInitialized(1)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleScore.valueOfInitialized(123),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getScore()).isEqualTo(123);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleScore.valueOf(-7, 123),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getScore()).isEqualTo(123);
                }
        );
    }

}
