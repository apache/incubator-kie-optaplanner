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

package org.optaplanner.core.api.score.buildin.simpledouble;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleDoubleScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(SimpleDoubleScore.parseScore("-147.2"))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(-147.2));
        assertThat(SimpleDoubleScore.parseScore("-7init/-147.2"))
                .isEqualTo(SimpleDoubleScore.valueOf(-7, -147.2));
    }

    @Test
    public void testToString() {
        assertThat(SimpleDoubleScore.valueOfInitialized(-147.2).toString()).isEqualTo("-147.2");
        assertEquals("-7init/-147.2", SimpleDoubleScore.valueOf(-7, -147.2).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        SimpleDoubleScore.parseScore("-147.2hard/-258.3soft");
    }

    @Test
    public void toInitializedScore() {
        assertThat(SimpleDoubleScore.valueOfInitialized(-147.2).toInitializedScore())
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(-147.2));
        assertThat(SimpleDoubleScore.valueOf(-7, -147.2).toInitializedScore())
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(-147.2));
    }

    @Test
    public void add() {
        assertThat(SimpleDoubleScore.valueOfInitialized(20.0).add(
                        SimpleDoubleScore.valueOfInitialized(-1.0)))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(19.0));
        assertThat(SimpleDoubleScore.valueOf(-70, 20.0).add(
                        SimpleDoubleScore.valueOf(-7, -1.0)))
                .isEqualTo(SimpleDoubleScore.valueOf(-77, 19.0));
    }

    @Test
    public void subtract() {
        assertThat(SimpleDoubleScore.valueOfInitialized(20.0).subtract(
                        SimpleDoubleScore.valueOfInitialized(-1.0)))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(21.0));
        assertThat(SimpleDoubleScore.valueOf(-70, 20.0).subtract(
                        SimpleDoubleScore.valueOf(-7, -1.0)))
                .isEqualTo(SimpleDoubleScore.valueOf(-63, 21.0));
    }

    @Test
    public void multiply() {
        assertThat(SimpleDoubleScore.valueOfInitialized(5.0).multiply(1.2))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(6.0));
        assertThat(SimpleDoubleScore.valueOfInitialized(1.0).multiply(1.2))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(1.2));
        assertThat(SimpleDoubleScore.valueOfInitialized(4.0).multiply(1.2))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(4.8));
        assertThat(SimpleDoubleScore.valueOf(-7, 4.3).multiply(2.0))
                .isEqualTo(SimpleDoubleScore.valueOf(-14, 8.6));
    }

    @Test
    public void divide() {
        assertThat(SimpleDoubleScore.valueOfInitialized(25.0).divide(5.0))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(5.0));
        assertThat(SimpleDoubleScore.valueOfInitialized(21.0).divide(5.0))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(4.2));
        assertThat(SimpleDoubleScore.valueOfInitialized(24.0).divide(5.0))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(4.8));
        assertThat(SimpleDoubleScore.valueOf(-14, 8.6).divide(2.0))
                .isEqualTo(SimpleDoubleScore.valueOf(-7, 4.3));
    }

    @Test
    public void power() {
        assertThat(SimpleDoubleScore.valueOfInitialized(1.5).power(2.0))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(2.25));
        assertThat(SimpleDoubleScore.valueOfInitialized(2.25).power(0.5))
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(1.5));
        assertThat(SimpleDoubleScore.valueOf(-7, 5.0).power(3.0))
                .isEqualTo(SimpleDoubleScore.valueOf(-343, 125.0));
    }

    @Test
    public void negate() {
        assertThat(SimpleDoubleScore.valueOfInitialized(1.5).negate())
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(-1.5));
        assertThat(SimpleDoubleScore.valueOfInitialized(-1.5).negate())
                .isEqualTo(SimpleDoubleScore.valueOfInitialized(1.5));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                SimpleDoubleScore.valueOfInitialized(-10.0),
                SimpleDoubleScore.valueOfInitialized(-10.0),
                SimpleDoubleScore.valueOf(0, -10.0)
        );
        assertScoresEqualsAndHashCode(
                SimpleDoubleScore.valueOf(-7, -10.0),
                SimpleDoubleScore.valueOf(-7, -10.0)
        );
        assertScoresNotEquals(
                SimpleDoubleScore.valueOfInitialized(-10.0),
                SimpleDoubleScore.valueOfInitialized(-30.0),
                SimpleDoubleScore.valueOf(-7, -10.0)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                SimpleDoubleScore.valueOf(-8, -0.0),
                SimpleDoubleScore.valueOf(-7, -20.0),
                SimpleDoubleScore.valueOf(-7, -1.0),
                SimpleDoubleScore.valueOf(-7, 0.0),
                SimpleDoubleScore.valueOf(-7, 1.0),
                SimpleDoubleScore.valueOfInitialized(-300.5),
                SimpleDoubleScore.valueOfInitialized(-300.0),
                SimpleDoubleScore.valueOfInitialized(-20.06),
                SimpleDoubleScore.valueOfInitialized(-20.007),
                SimpleDoubleScore.valueOfInitialized(-20.0),
                SimpleDoubleScore.valueOfInitialized(-1.0),
                SimpleDoubleScore.valueOfInitialized(0.0),
                SimpleDoubleScore.valueOfInitialized(1.0)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleDoubleScore.valueOfInitialized(123.4),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertEquals(123.4, output.getScore(), 0.0);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleDoubleScore.valueOf(-7, 123.4),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertEquals(123.4, output.getScore(), 0.0);
                }
        );
    }

}
