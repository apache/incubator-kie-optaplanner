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

package org.optaplanner.core.api.score.buildin.bendable;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.score.buildin.bendable.BendableScoreDefinition;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class BendableScoreTest extends AbstractScoreTest {

    private BendableScoreDefinition scoreDefinitionHSS = new BendableScoreDefinition(1, 2);
    private BendableScoreDefinition scoreDefinitionHHH = new BendableScoreDefinition(3, 0);
    private BendableScoreDefinition scoreDefinitionSSS = new BendableScoreDefinition(0, 3);

    @Test
    public void parseScore() {
        assertThat(scoreDefinitionHSS.parseScore("[-147]hard/[-258/-369]soft"))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-147, -258, -369));
        assertThat(scoreDefinitionHHH.parseScore("[-147/-258/-369]hard/[]soft"))
                .isEqualTo(scoreDefinitionHHH.createScoreInitialized(-147, -258, -369));
        assertThat(scoreDefinitionSSS.parseScore("[]hard/[-147/-258/-369]soft"))
                .isEqualTo(scoreDefinitionSSS.createScoreInitialized(-147, -258, -369));
        assertThat(scoreDefinitionSSS.parseScore("-7init/[]hard/[-147/-258/-369]soft"))
                .isEqualTo(scoreDefinitionSSS.createScore(-7, -147, -258, -369));
    }

    @Test
    public void testToString() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(-147, -258, -369)).hasToString("[-147]hard/[-258/-369]soft");
        assertThat(scoreDefinitionHHH.createScoreInitialized(-147, -258, -369)).hasToString("[-147/-258/-369]hard/[]soft");
        assertThat(scoreDefinitionSSS.createScoreInitialized(-147, -258, -369)).hasToString("[]hard/[-147/-258/-369]soft");
        assertThat(scoreDefinitionSSS.createScore(-7, -147, -258, -369)).hasToString("-7init/[]hard/[-147/-258/-369]soft");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        scoreDefinitionHSS.parseScore("-147");
    }

    @Test
    public void getHardOrSoftScore() {
        BendableScore initializedScore = scoreDefinitionHSS.createScoreInitialized(-5, -10, -200);
        assertThat(initializedScore.getHardOrSoftScore(0)).isEqualTo(-5);
        assertThat(initializedScore.getHardOrSoftScore(1)).isEqualTo(-10);
        assertThat(initializedScore.getHardOrSoftScore(2)).isEqualTo(-200);
    }

    @Test
    public void toInitializedScoreHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(-147, -258, -369).toInitializedScore())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-147, -258, -369));
        assertThat(scoreDefinitionHSS.createScore(-7, -147, -258, -369).toInitializedScore())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-147, -258, -369));
    }

    @Test
    public void feasibleHSS() {
        assertScoreNotFeasible(
                scoreDefinitionHSS.createScoreInitialized(-5, -300, -4000),
                scoreDefinitionHSS.createScore(-7, -5, -300, -4000),
                scoreDefinitionHSS.createScore(-7, -5, -300, -4000)
        );
        assertScoreFeasible(
                scoreDefinitionHSS.createScoreInitialized(0, -300, -4000),
                scoreDefinitionHSS.createScoreInitialized(2, -300, -4000),
                scoreDefinitionHSS.createScore(0, 0, -300, -4000)
                );
    }

    @Test
    public void addHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(20, -20, -4000).add(
                        scoreDefinitionHSS.createScoreInitialized(-1, -300, 4000)))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(19, -320, 0));
        assertThat(scoreDefinitionHSS.createScore(-70, 20, -20, -4000).add(
                        scoreDefinitionHSS.createScore(-7, -1, -300, 4000)))
                .isEqualTo(scoreDefinitionHSS.createScore(-77, 19, -320, 0));
    }

    @Test
    public void subtractHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(20, -20, -4000).subtract(
                        scoreDefinitionHSS.createScoreInitialized(-1, -300, 4000)))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(21, 280, -8000));
        assertThat(scoreDefinitionHSS.createScore(-70, 20, -20, -4000).subtract(
                        scoreDefinitionHSS.createScore(-7, -1, -300, 4000)))
                .isEqualTo(scoreDefinitionHSS.createScore(-63, 21, 280, -8000));
    }

    @Test
    public void multiplyHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(5, -5, 5).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(6, -6, 6));
        assertThat(scoreDefinitionHSS.createScoreInitialized(1, -1, 1).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(1, -2, 1));
        assertThat(scoreDefinitionHSS.createScoreInitialized(4, -4, 4).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4, -5, 4));
        assertThat(scoreDefinitionHSS.createScore(-7, 4, -5, 6).multiply(2.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-14, 8, -10, 12));
    }

    @Test
    public void divideHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(25, -25, 25).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(5, -5, 5));
        assertThat(scoreDefinitionHSS.createScoreInitialized(21, -21, 21).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4, -5, 4));
        assertThat(scoreDefinitionHSS.createScoreInitialized(24, -24, 24).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4, -5, 4));
        assertThat(scoreDefinitionHSS.createScore(-14, 8, -10, 12).divide(2.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-7, 4, -5, 6));
    }

    @Test
    public void powerHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(3, -4, 5).power(2.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(9, 16, 25));
        assertThat(scoreDefinitionHSS.createScoreInitialized(9, 16, 25).power(0.5))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(3, 4, 5));
        assertThat(scoreDefinitionHSS.createScore(-7, 3, -4, 5).power(3.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-343, 27, -64, 125));
    }

    @Test
    public void negateHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(3, -4, 5).negate())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-3, 4, -5));
        assertThat(scoreDefinitionHSS.createScoreInitialized(-3, 4, -5).negate())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(3, -4, 5));
    }

    @Test
    public void equalsAndHashCodeHSS() {
        assertScoresEqualsAndHashCode(
                scoreDefinitionHSS.createScoreInitialized(-10, -200, -3000),
                scoreDefinitionHSS.createScoreInitialized(-10, -200, -3000),
                scoreDefinitionHSS.createScore(0, -10, -200, -3000)
        );
        assertScoresEqualsAndHashCode(
                scoreDefinitionHSS.createScore(-7, -10, -200, -3000),
                scoreDefinitionHSS.createScore(-7, -10, -200, -3000)
        );
        assertScoresNotEquals(
                scoreDefinitionHSS.createScoreInitialized(-10, -200, -3000),
                scoreDefinitionHSS.createScoreInitialized(-30, -200, -3000),
                scoreDefinitionHSS.createScoreInitialized(-10, -400, -3000),
                scoreDefinitionHSS.createScoreInitialized(-10, -400, -5000),
                scoreDefinitionHSS.createScore(-7, -10, -200, -3000)
        );
    }

    @Test
    public void compareToHSS() {
        assertThat(Arrays.asList(
                scoreDefinitionHSS.createScore(-8, 0, 0, 0),
                scoreDefinitionHSS.createScore(-7, -20, -20, -20),
                scoreDefinitionHSS.createScore(-7, -1, -300, -4000),
                scoreDefinitionHSS.createScore(-7, 0, 0, 0),
                scoreDefinitionHSS.createScore(-7, 0, 0, 1),
                scoreDefinitionHSS.createScore(-7, 0, 1, 0),
                scoreDefinitionHSS.createScoreInitialized(-20, Integer.MIN_VALUE, Integer.MIN_VALUE),
                scoreDefinitionHSS.createScoreInitialized(-20, Integer.MIN_VALUE, -20),
                scoreDefinitionHSS.createScoreInitialized(-20, Integer.MIN_VALUE, 1),
                scoreDefinitionHSS.createScoreInitialized(-20, -300, -4000),
                scoreDefinitionHSS.createScoreInitialized(-20, -300, -300),
                scoreDefinitionHSS.createScoreInitialized(-20, -300, -20),
                scoreDefinitionHSS.createScoreInitialized(-20, -300, 300),
                scoreDefinitionHSS.createScoreInitialized(-20, -20, -300),
                scoreDefinitionHSS.createScoreInitialized(-20, -20, 0),
                scoreDefinitionHSS.createScoreInitialized(-20, -20, 1),
                scoreDefinitionHSS.createScoreInitialized(-1, -300, -4000),
                scoreDefinitionHSS.createScoreInitialized(-1, -300, -20),
                scoreDefinitionHSS.createScoreInitialized(-1, -20, -300),
                scoreDefinitionHSS.createScoreInitialized(1, Integer.MIN_VALUE, -20),
                scoreDefinitionHSS.createScoreInitialized(1, -20, Integer.MIN_VALUE)
        )).isSorted();
    }

    private BendableScoreDefinition scoreDefinitionHHSSS = new BendableScoreDefinition(2, 3);

    @Test
    public void feasibleHHSSS() {
        assertScoreNotFeasible(
                scoreDefinitionHHSSS.createScoreInitialized(-5, 0, -300, -4000, -5000),
                scoreDefinitionHHSSS.createScoreInitialized(0, -5, -300, -4000, -5000)
        );
        assertScoreFeasible(
                scoreDefinitionHHSSS.createScoreInitialized(0, 0, -300, -4000, -5000),
                scoreDefinitionHHSSS.createScoreInitialized(0, 2, -300, -4000, -5000),
                scoreDefinitionHHSSS.createScoreInitialized(2, 0, -300, -4000, -5000)
        );
    }

    @Test
    public void addHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(20, -20, -4000, 0, 0).add(
                        scoreDefinitionHHSSS.createScoreInitialized(-1, -300, 4000, 0, 0)))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(19, -320, 0, 0, 0));
    }

    @Test
    public void subtractHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(20, -20, -4000, 0, 0).subtract(
                        scoreDefinitionHHSSS.createScoreInitialized(-1, -300, 4000, 0, 0)))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(21, 280, -8000, 0, 0));
    }

    @Test
    public void multiplyHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(5, -5, 5, 0, 0).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(6, -6, 6, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(1, -1, 1, 0, 0).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(1, -2, 1, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(4, -4, 4, 0, 0).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
    }

    @Test
    public void divideHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(25, -25, 25, 0, 0).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(5, -5, 5, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(21, -21, 21, 0, 0).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(24, -24, 24, 0, 0).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
    }

    @Test
    public void powerHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(3, -4, 5, 0, 0).power(2.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(9, 16, 25, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(9, 16, 25, 0, 0).power(0.5))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(3, 4, 5, 0, 0));
    }

    @Test
    public void negateHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(3, -4, 5, 0, 0).negate())
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(-3, 4, -5, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(-3, 4, -5, 0, 0).negate())
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(3, -4, 5, 0, 0));
    }

    @Test
    public void equalsAndHashCodeHHSSS() {
        assertScoresEqualsAndHashCode(
                scoreDefinitionHHSSS.createScoreInitialized(-10, -20, -30, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-10, -20, -30, 0, 0)
        );
    }

    @Test
    public void compareToHHSSS() {
        assertThat(Arrays.asList(
                scoreDefinitionHHSSS.createScoreInitialized(-20, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, Integer.MIN_VALUE, -20, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, Integer.MIN_VALUE, 1, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -300, -4000, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -300, -300, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -300, -20, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -300, 300, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -20, -300, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -20, 0, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-20, -20, 1, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-1, -300, -4000, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-1, -300, -20, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(-1, -20, -300, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(1, Integer.MIN_VALUE, -20, 0, 0),
                scoreDefinitionHHSSS.createScoreInitialized(1, -20, Integer.MIN_VALUE, 0, 0)
        )).isSorted();
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                scoreDefinitionHSS.createScoreInitialized(-12, 3400, -56),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore(0)).isEqualTo(-12);
                    assertThat(output.getSoftScore(0)).isEqualTo(3400);
                    assertThat(output.getSoftScore(1)).isEqualTo(-56);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                scoreDefinitionHSS.createScore(-7, -12, 3400, -56),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore(0)).isEqualTo(-12);
                    assertThat(output.getSoftScore(0)).isEqualTo(3400);
                    assertThat(output.getSoftScore(1)).isEqualTo(-56);
                }
        );
    }

}
