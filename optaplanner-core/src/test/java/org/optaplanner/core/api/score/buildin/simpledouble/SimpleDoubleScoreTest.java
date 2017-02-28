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

import static org.junit.Assert.*;

public class SimpleDoubleScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertEquals(SimpleDoubleScore.valueOf(-147.2),
                SimpleDoubleScore.parseScore("-147.2"));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-7, -147.2),
                SimpleDoubleScore.parseScore("-7init/-147.2"));
    }

    @Test
    public void toShortString() {
        assertEquals("0", SimpleDoubleScore.valueOf(0.0).toShortString());
        assertEquals("-147.2", SimpleDoubleScore.valueOf(-147.2).toShortString());
        assertEquals("-7init/-147.2", SimpleDoubleScore.valueOfUninitialized(-7, -147.2).toShortString());
        assertEquals("-7init", SimpleDoubleScore.valueOfUninitialized(-7, 0.0).toShortString());
    }

    @Test
    public void testToString() {
        assertEquals("0.0", SimpleDoubleScore.valueOf(0.0).toString());
        assertEquals("-147.2", SimpleDoubleScore.valueOf(-147.2).toString());
        assertEquals("-7init/-147.2", SimpleDoubleScore.valueOfUninitialized(-7, -147.2).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        SimpleDoubleScore.parseScore("-147.2hard/-258.3soft");
    }

    @Test
    public void toInitializedScore() {
        assertEquals(SimpleDoubleScore.valueOf(-147.2),
                SimpleDoubleScore.valueOf(-147.2).toInitializedScore());
        assertEquals(SimpleDoubleScore.valueOf(-147.2),
                SimpleDoubleScore.valueOfUninitialized(-7, -147.2).toInitializedScore());
    }

    @Test
    public void withInitScore() {
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-7, -147.2),
                SimpleDoubleScore.valueOf(-147.2).withInitScore(-7));
    }

    @Test
    public void add() {
        assertEquals(SimpleDoubleScore.valueOf(19.0),
                SimpleDoubleScore.valueOf(20.0).add(
                        SimpleDoubleScore.valueOf(-1.0)));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-77, 19.0),
                SimpleDoubleScore.valueOfUninitialized(-70, 20.0).add(
                        SimpleDoubleScore.valueOfUninitialized(-7, -1.0)));
    }

    @Test
    public void subtract() {
        assertEquals(SimpleDoubleScore.valueOf(21.0),
                SimpleDoubleScore.valueOf(20.0).subtract(
                        SimpleDoubleScore.valueOf(-1.0)));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-63, 21.0),
                SimpleDoubleScore.valueOfUninitialized(-70, 20.0).subtract(
                        SimpleDoubleScore.valueOfUninitialized(-7, -1.0)));
    }

    @Test
    public void multiply() {
        assertEquals(SimpleDoubleScore.valueOf(6.0),
                SimpleDoubleScore.valueOf(5.0).multiply(1.2));
        assertEquals(SimpleDoubleScore.valueOf(1.2),
                SimpleDoubleScore.valueOf(1.0).multiply(1.2));
        assertEquals(SimpleDoubleScore.valueOf(4.8),
                SimpleDoubleScore.valueOf(4.0).multiply(1.2));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-14, 8.6),
                SimpleDoubleScore.valueOfUninitialized(-7, 4.3).multiply(2.0));
    }

    @Test
    public void divide() {
        assertEquals(SimpleDoubleScore.valueOf(5.0),
                SimpleDoubleScore.valueOf(25.0).divide(5.0));
        assertEquals(SimpleDoubleScore.valueOf(4.2),
                SimpleDoubleScore.valueOf(21.0).divide(5.0));
        assertEquals(SimpleDoubleScore.valueOf(4.8),
                SimpleDoubleScore.valueOf(24.0).divide(5.0));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-7, 4.3),
                SimpleDoubleScore.valueOfUninitialized(-14, 8.6).divide(2.0));
    }

    @Test
    public void power() {
        assertEquals(SimpleDoubleScore.valueOf(2.25),
                SimpleDoubleScore.valueOf(1.5).power(2.0));
        assertEquals(SimpleDoubleScore.valueOf(1.5),
                SimpleDoubleScore.valueOf(2.25).power(0.5));
        assertEquals(SimpleDoubleScore.valueOfUninitialized(-343, 125.0),
                SimpleDoubleScore.valueOfUninitialized(-7, 5.0).power(3.0));
    }

    @Test
    public void negate() {
        assertEquals(SimpleDoubleScore.valueOf(-1.5),
                SimpleDoubleScore.valueOf(1.5).negate());
        assertEquals(SimpleDoubleScore.valueOf(1.5),
                SimpleDoubleScore.valueOf(-1.5).negate());
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                SimpleDoubleScore.valueOf(-10.0),
                SimpleDoubleScore.valueOf(-10.0),
                SimpleDoubleScore.valueOfUninitialized(0, -10.0)
        );
        assertScoresEqualsAndHashCode(
                SimpleDoubleScore.valueOfUninitialized(-7, -10.0),
                SimpleDoubleScore.valueOfUninitialized(-7, -10.0)
        );
        assertScoresNotEquals(
                SimpleDoubleScore.valueOf(-10.0),
                SimpleDoubleScore.valueOf(-30.0),
                SimpleDoubleScore.valueOfUninitialized(-7, -10.0)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                SimpleDoubleScore.valueOfUninitialized(-8, -0.0),
                SimpleDoubleScore.valueOfUninitialized(-7, -20.0),
                SimpleDoubleScore.valueOfUninitialized(-7, -1.0),
                SimpleDoubleScore.valueOfUninitialized(-7, 0.0),
                SimpleDoubleScore.valueOfUninitialized(-7, 1.0),
                SimpleDoubleScore.valueOf(-300.5),
                SimpleDoubleScore.valueOf(-300.0),
                SimpleDoubleScore.valueOf(-20.06),
                SimpleDoubleScore.valueOf(-20.007),
                SimpleDoubleScore.valueOf(-20.0),
                SimpleDoubleScore.valueOf(-1.0),
                SimpleDoubleScore.valueOf(0.0),
                SimpleDoubleScore.valueOf(1.0)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleDoubleScore.valueOf(123.4),
                output -> {
                    assertEquals(0, output.getInitScore());
                    assertEquals(123.4, output.getScore(), 0.0);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                SimpleDoubleScore.valueOfUninitialized(-7, 123.4),
                output -> {
                    assertEquals(-7, output.getInitScore());
                    assertEquals(123.4, output.getScore(), 0.0);
                }
        );
    }

}
