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

package org.optaplanner.core.api.score.buildin.simplelong;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleLongScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertThat(SimpleLongScore.parseScore("-147"))
                .isEqualTo(SimpleLongScore.valueOfInitialized(-147L));
        assertThat(SimpleLongScore.parseScore("-7init/-147"))
                .isEqualTo(SimpleLongScore.valueOf(-7, -147L));
    }

    @Test
    public void testToString() {
        assertThat(SimpleLongScore.valueOfInitialized(-147L).toString()).isEqualTo("-147");
        assertThat(SimpleLongScore.valueOf(-7, -147L).toString()).isEqualTo("-7init/-147");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        SimpleLongScore.parseScore("-147hard/-258soft");
    }

    @Test
    public void toInitializedScore() {
        assertThat(SimpleLongScore.valueOfInitialized(-147L).toInitializedScore())
                .isEqualTo(SimpleLongScore.valueOfInitialized(-147L));
        assertThat(SimpleLongScore.valueOf(-7, -147L).toInitializedScore())
                .isEqualTo(SimpleLongScore.valueOfInitialized(-147L));
    }

    @Test
    public void add() {
        assertThat(SimpleLongScore.valueOfInitialized(20L).add(
                        SimpleLongScore.valueOfInitialized(-1L)))
                .isEqualTo(SimpleLongScore.valueOfInitialized(19L));
        assertThat(SimpleLongScore.valueOf(-70, 20L).add(
                        SimpleLongScore.valueOf(-7, -1L)))
                .isEqualTo(SimpleLongScore.valueOf(-77, 19L));
    }

    @Test
    public void subtract() {
        assertThat(SimpleLongScore.valueOfInitialized(20L).subtract(
                        SimpleLongScore.valueOfInitialized(-1L)))
                .isEqualTo(SimpleLongScore.valueOfInitialized(21L));
        assertThat(SimpleLongScore.valueOf(-70, 20L).subtract(
                        SimpleLongScore.valueOf(-7, -1L)))
                .isEqualTo(SimpleLongScore.valueOf(-63, 21L));
    }

    @Test
    public void multiply() {
        assertThat(SimpleLongScore.valueOfInitialized(5L).multiply(1.2))
                .isEqualTo(SimpleLongScore.valueOfInitialized(6L));
        assertThat(SimpleLongScore.valueOfInitialized(1L).multiply(1.2))
                .isEqualTo(SimpleLongScore.valueOfInitialized(1L));
        assertThat(SimpleLongScore.valueOfInitialized(4L).multiply(1.2))
                .isEqualTo(SimpleLongScore.valueOfInitialized(4L));
        assertThat(SimpleLongScore.valueOf(-7, 4L).multiply(2.0))
                .isEqualTo(SimpleLongScore.valueOf(-14, 8L));
    }

    @Test
    public void divide() {
        assertThat(SimpleLongScore.valueOfInitialized(25L).divide(5.0))
                .isEqualTo(SimpleLongScore.valueOfInitialized(5L));
        assertThat(SimpleLongScore.valueOfInitialized(21L).divide(5.0))
                .isEqualTo(SimpleLongScore.valueOfInitialized(4L));
        assertThat(SimpleLongScore.valueOfInitialized(24L).divide(5.0))
                .isEqualTo(SimpleLongScore.valueOfInitialized(4L));
        assertThat(SimpleLongScore.valueOf(-14, 8L).divide(2.0))
                .isEqualTo(SimpleLongScore.valueOf(-7, 4L));
    }

    @Test
    public void power() {
        assertThat(SimpleLongScore.valueOfInitialized(5L).power(2.0))
                .isEqualTo(SimpleLongScore.valueOfInitialized(25L));
        assertThat(SimpleLongScore.valueOfInitialized(25L).power(0.5))
                .isEqualTo(SimpleLongScore.valueOfInitialized(5L));
        assertThat(SimpleLongScore.valueOf(-7, 5L).power(3.0))
                .isEqualTo(SimpleLongScore.valueOf(-343, 125L));
    }

    @Test
    public void negate() {
        assertThat(SimpleLongScore.valueOfInitialized(5L).negate())
                .isEqualTo(SimpleLongScore.valueOfInitialized(-5L));
        assertThat(SimpleLongScore.valueOfInitialized(-5L).negate())
                .isEqualTo(SimpleLongScore.valueOfInitialized(5L));
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                SimpleLongScore.valueOfInitialized(-10L),
                SimpleLongScore.valueOfInitialized(-10L),
                SimpleLongScore.valueOf(0, -10L)
        );
        assertScoresEqualsAndHashCode(
                SimpleLongScore.valueOf(-7, -10L),
                SimpleLongScore.valueOf(-7, -10L)
        );
        assertScoresNotEquals(
                SimpleLongScore.valueOfInitialized(-10L),
                SimpleLongScore.valueOfInitialized(-30L),
                SimpleLongScore.valueOf(-7, -10L)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                SimpleLongScore.valueOf(-8, 0L),
                SimpleLongScore.valueOf(-7, -20L),
                SimpleLongScore.valueOf(-7, -1L),
                SimpleLongScore.valueOf(-7, 0L),
                SimpleLongScore.valueOf(-7, 1L),
                SimpleLongScore.valueOfInitialized(((long) Integer.MIN_VALUE) - 4000L),
                SimpleLongScore.valueOfInitialized(-300L),
                SimpleLongScore.valueOfInitialized(-20L),
                SimpleLongScore.valueOfInitialized(-1L),
                SimpleLongScore.valueOfInitialized(0L),
                SimpleLongScore.valueOfInitialized(1L),
                SimpleLongScore.valueOfInitialized(((long) Integer.MAX_VALUE) + 4000L)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleLongScore.valueOfInitialized(123L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getScore()).isEqualTo(123L);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleLongScore.valueOf(-7, 123L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getScore()).isEqualTo(123L);
                }
        );
    }

}
