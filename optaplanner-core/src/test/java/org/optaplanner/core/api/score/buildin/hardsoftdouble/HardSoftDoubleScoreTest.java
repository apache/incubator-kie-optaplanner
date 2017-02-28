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

import static org.junit.Assert.*;

public class HardSoftDoubleScoreTest extends AbstractScoreTest {

    @Test
    public void parseScore() {
        assertEquals(HardSoftDoubleScore.valueOf(-147.2, -258.3),
                HardSoftDoubleScore.parseScore("-147.2hard/-258.3soft"));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-7, -147.2, -258.3),
                HardSoftDoubleScore.parseScore("-7init/-147.2hard/-258.3soft"));
    }

    @Test
    public void toShortString() {
        assertEquals("0", HardSoftDoubleScore.valueOf(0.0, 0.0).toShortString());
        assertEquals("-258.3soft", HardSoftDoubleScore.valueOf(0.0, -258.3).toShortString());
        assertEquals("-147.2hard", HardSoftDoubleScore.valueOf(-147.2, 0.0).toShortString());
        assertEquals("-147.2hard/-258.3soft", HardSoftDoubleScore.valueOf(-147.2, -258.3).toShortString());
        assertEquals("-7init", HardSoftDoubleScore.valueOfUninitialized(-7, 0.0, 0.0).toShortString());
        assertEquals("-7init/-258.3soft", HardSoftDoubleScore.valueOfUninitialized(-7, 0.0, -258.3).toShortString());
        assertEquals("-7init/-147.2hard/-258.3soft", HardSoftDoubleScore.valueOfUninitialized(-7, -147.2, -258.3).toShortString());
    }

    @Test
    public void testToString() {
        assertEquals("0.0hard/-258.3soft", HardSoftDoubleScore.valueOf(0.0, -258.3).toString());
        assertEquals("-147.2hard/-258.3soft", HardSoftDoubleScore.valueOf(-147.2, -258.3).toString());
        assertEquals("-7init/-147.2hard/-258.3soft", HardSoftDoubleScore.valueOfUninitialized(-7, -147.2, -258.3).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        HardSoftDoubleScore.parseScore("-147.2");
    }

    @Test
    public void toInitializedScore() {
        assertEquals(HardSoftDoubleScore.valueOf(-147.2, -258.3),
                HardSoftDoubleScore.valueOf(-147.2, -258.3).toInitializedScore());
        assertEquals(HardSoftDoubleScore.valueOf(-147.2, -258.3),
                HardSoftDoubleScore.valueOfUninitialized(-7, -147.2, -258.3).toInitializedScore());
    }

    @Test
    public void withInitScore() {
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-7, -147.2, -258.3),
                HardSoftDoubleScore.valueOf(-147.2, -258.3).withInitScore(-7));
    }

    @Test
    public void feasible() {
        assertScoreNotFeasible(
                HardSoftDoubleScore.valueOf(-5.0, -300.0),
                HardSoftDoubleScore.valueOf(-5.0, 4000.0),
                HardSoftDoubleScore.valueOf(-0.007, 4000.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -5.0, -300.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, 0.0, -300.0)
        );
        assertScoreFeasible(
                HardSoftDoubleScore.valueOf(0.0, -300.007),
                HardSoftDoubleScore.valueOf(0.0, -300.0),
                HardSoftDoubleScore.valueOf(2.0, -300.0),
                HardSoftDoubleScore.valueOfUninitialized(0, 0.0, -300.0)
        );
    }

    @Test
    public void add() {
        assertEquals(HardSoftDoubleScore.valueOf(19.0, -320.0),
                HardSoftDoubleScore.valueOf(20.0, -20.0).add(
                        HardSoftDoubleScore.valueOf(-1.0, -300.0)));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-77, 19.0, -320.0),
                HardSoftDoubleScore.valueOfUninitialized(-70, 20.0, -20.0).add(
                        HardSoftDoubleScore.valueOfUninitialized(-7, -1.0, -300.0)));
    }

    @Test
    public void subtract() {
        assertEquals(HardSoftDoubleScore.valueOf(21.0, 280.0),
                HardSoftDoubleScore.valueOf(20.0, -20.0).subtract(
                        HardSoftDoubleScore.valueOf(-1.0, -300.0)));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-63, 21.0, 280.0),
                HardSoftDoubleScore.valueOfUninitialized(-70, 20.0, -20.0).subtract(
                        HardSoftDoubleScore.valueOfUninitialized(-7, -1.0, -300.0)));
    }

    @Test
    public void multiply() {
        assertEquals(HardSoftDoubleScore.valueOf(6.0, -6.0),
                HardSoftDoubleScore.valueOf(5.0, -5.0).multiply(1.2));
        assertEquals(HardSoftDoubleScore.valueOf(1.2, -1.2),
                HardSoftDoubleScore.valueOf(1.0, -1.0).multiply(1.2));
        assertEquals(HardSoftDoubleScore.valueOf(4.8, -4.8),
                HardSoftDoubleScore.valueOf(4.0, -4.0).multiply(1.2));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-14, 8.6, -10.4),
                HardSoftDoubleScore.valueOfUninitialized(-7, 4.3, -5.2).multiply(2.0));
    }

    @Test
    public void divide() {
        assertEquals(HardSoftDoubleScore.valueOf(5.0, -5.0),
                HardSoftDoubleScore.valueOf(25.0, -25.0).divide(5.0));
        assertEquals(HardSoftDoubleScore.valueOf(4.2, -4.2),
                HardSoftDoubleScore.valueOf(21.0, -21.0).divide(5.0));
        assertEquals(HardSoftDoubleScore.valueOf(4.8, -4.8),
                HardSoftDoubleScore.valueOf(24.0, -24.0).divide(5.0));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-7, 4.3, -5.2),
                HardSoftDoubleScore.valueOfUninitialized(-14, 8.6, -10.4).divide(2.0));
    }

    @Test
    public void power() {
        assertEquals(HardSoftDoubleScore.valueOf(16.0, 2.25),
                HardSoftDoubleScore.valueOf(-4.0, 1.5).power(2.0));
        assertEquals(HardSoftDoubleScore.valueOf(4.0, 1.5),
                HardSoftDoubleScore.valueOf(16.0, 2.25).power(0.5));
        assertEquals(HardSoftDoubleScore.valueOfUninitialized(-343, -64.0, 125.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -4.0, 5.0).power(3.0));
    }

    @Test
    public void negate() {
        assertEquals(HardSoftDoubleScore.valueOf(-4.0, 1.5),
                HardSoftDoubleScore.valueOf(4.0, -1.5).negate());
        assertEquals(HardSoftDoubleScore.valueOf(4.0, -1.5),
                HardSoftDoubleScore.valueOf(-4.0, 1.5).negate());
    }

    @Test
    public void equalsAndHashCode() {
        assertScoresEqualsAndHashCode(
                HardSoftDoubleScore.valueOf(-10.0, -200.0),
                HardSoftDoubleScore.valueOf(-10.0, -200.0),
                HardSoftDoubleScore.valueOfUninitialized(0, -10.0, -200.0)
        );
        assertScoresEqualsAndHashCode(
                HardSoftDoubleScore.valueOfUninitialized(-7, -10.0, -200.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -10.0, -200.0)
        );
        assertScoresNotEquals(
                HardSoftDoubleScore.valueOf(-10.0, -200.0),
                HardSoftDoubleScore.valueOf(-30.0, -200.0),
                HardSoftDoubleScore.valueOf(-10.0, -400.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -10.0, -200.0)
        );
    }

    @Test
    public void compareTo() {
        PlannerAssert.assertCompareToOrder(
                HardSoftDoubleScore.valueOfUninitialized(-8, 0.0, 0.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -20.0, -20.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, -1.0, -300.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, 0.0, 0.0),
                HardSoftDoubleScore.valueOfUninitialized(-7, 0.0, 1.0),
                HardSoftDoubleScore.valueOf(-20.06, -20.0),
                HardSoftDoubleScore.valueOf(-20.007, -20.0),
                HardSoftDoubleScore.valueOf(-20.0, -Double.MAX_VALUE),
                HardSoftDoubleScore.valueOf(-20.0, -20.06),
                HardSoftDoubleScore.valueOf(-20.0, -20.007),
                HardSoftDoubleScore.valueOf(-20.0, -20.0),
                HardSoftDoubleScore.valueOf(-1.0, -300.0),
                HardSoftDoubleScore.valueOf(-1.0, 4000.0),
                HardSoftDoubleScore.valueOf(0.0, -1.0),
                HardSoftDoubleScore.valueOf(0.0, 0.0),
                HardSoftDoubleScore.valueOf(0.0, 1.0)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftDoubleScore.valueOf(-12.3, 3400.5),
                output -> {
                    assertEquals(0, output.getInitScore());
                    assertEquals(-12.3, output.getHardScore(), 0.0);
                    assertEquals(3400.5, output.getSoftScore(), 0.0);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                HardSoftDoubleScore.valueOfUninitialized(-7, -12.3, 3400.5),
                output -> {
                    assertEquals(-7, output.getInitScore());
                    assertEquals(-12.3, output.getHardScore(), 0.0);
                    assertEquals(3400.5, output.getSoftScore(), 0.0);
                }
        );
    }

}
