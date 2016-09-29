/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.impl.measurement;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreDifferencePercentageTest {

    @Test(expected = IllegalStateException.class)
    public void calculateScoreDifferencePercentageException() {
        BendableScore score1 = BendableScore.valueOfInitialized(new int[]{1, 2, 3}, new int[]{4, 5});
        BendableScore score2 = BendableScore.valueOfInitialized(new int[]{1, 2}, new int[]{4, 5});
        ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
    }

    @Test
    public void calculateScoreDifferencePercentage() {
        Offset<Double> tolerance = Assertions.offset(0.00001);
        SimpleScore score1 = SimpleScore.valueOfInitialized(-100);
        SimpleScore score2 = SimpleScore.valueOfInitialized(-100);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.0, tolerance);

        score1 = SimpleScore.valueOfInitialized(100);
        score2 = SimpleScore.valueOfInitialized(100);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.0, tolerance);

        score1 = SimpleScore.valueOfInitialized(-100);
        score2 = SimpleScore.valueOfInitialized(-10);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.9, tolerance);

        score1 = SimpleScore.valueOfInitialized(100);
        score2 = SimpleScore.valueOfInitialized(10);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(-0.9, tolerance);

        score1 = SimpleScore.valueOfInitialized(-100);
        score2 = SimpleScore.valueOfInitialized(-1);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.99, tolerance);

        score1 = SimpleScore.valueOfInitialized(100);
        score2 = SimpleScore.valueOfInitialized(1);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(-0.99, tolerance);

        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-100, -1);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.0, tolerance);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[1]).isEqualTo(0.0, tolerance);

        hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -100);
        hardSoftScore2 = HardSoftScore.valueOfInitialized(-1, -10);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(0.99, tolerance);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[1]).isEqualTo(0.9, tolerance);

        hardSoftScore1 = HardSoftScore.valueOfInitialized(100, 100);
        hardSoftScore2 = HardSoftScore.valueOfInitialized(1, 10);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(-0.99, tolerance);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[1]).isEqualTo(-0.9, tolerance);

        hardSoftScore1 = HardSoftScore.valueOfInitialized(100, -100);
        hardSoftScore2 = HardSoftScore.valueOfInitialized(-100, 200);
        scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[0]).isEqualTo(-2, tolerance);
        assertThat(scoreDifferencePercentage.getPercentageLevels()[1]).isEqualTo(3, tolerance);
    }

    @Test
    public void add() {
        Offset<Double> tolerance = Assertions.offset(0.00001);
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage2 =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        double[] levels = scoreDifferencePercentage.add(scoreDifferencePercentage2).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(-2.0, tolerance);
        assertThat(levels[1]).isEqualTo(-18.0, tolerance);
    }

    @Test
    public void subtract() {
        Offset<Double> tolerance = Assertions.offset(0.00001);
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage2 =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        double[] levels = scoreDifferencePercentage.subtract(scoreDifferencePercentage2).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(0.0, tolerance);
        assertThat(levels[1]).isEqualTo(0.0, tolerance);
    }

    @Test
    public void multiply() {
        Offset<Double> tolerance = Assertions.offset(0.00001);
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        double[] levels = scoreDifferencePercentage.multiply(3.14).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(-3.14, tolerance);
        assertThat(levels[1]).isEqualTo(-28.26, tolerance);

        levels = scoreDifferencePercentage.multiply(-1).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(1, tolerance);
        assertThat(levels[1]).isEqualTo(9.0, tolerance);
    }

    @Test
    public void divide() {
        Offset<Double> tolerance = Assertions.offset(0.00001);
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        double[] levels = scoreDifferencePercentage.multiply(0.5).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(-0.5, tolerance);
        assertThat(levels[1]).isEqualTo(-4.5, tolerance);

        levels = scoreDifferencePercentage.multiply(-1).getPercentageLevels();
        assertThat(levels[0]).isEqualTo(1, tolerance);
        assertThat(levels[1]).isEqualTo(9.0, tolerance);
    }

    @Test(expected = IllegalStateException.class)
    public void addWithWrongDimension() {
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        SimpleScore score1 = SimpleScore.valueOfInitialized(-100);
        SimpleScore score2 = SimpleScore.valueOfInitialized(-200);
        ScoreDifferencePercentage scoreDifferencePercentage2 =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);

        scoreDifferencePercentage.add(scoreDifferencePercentage2);
    }

    @Test(expected = IllegalStateException.class)
    public void subtractWithWrongDimension() {
        HardSoftScore hardSoftScore1 = HardSoftScore.valueOfInitialized(-100, -1);
        HardSoftScore hardSoftScore2 = HardSoftScore.valueOfInitialized(-200, -10);
        ScoreDifferencePercentage scoreDifferencePercentage =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(hardSoftScore1, hardSoftScore2);

        SimpleScore score1 = SimpleScore.valueOfInitialized(-100);
        SimpleScore score2 = SimpleScore.valueOfInitialized(-200);
        ScoreDifferencePercentage scoreDifferencePercentage2 =
                ScoreDifferencePercentage.calculateScoreDifferencePercentage(score1, score2);

        scoreDifferencePercentage.subtract(scoreDifferencePercentage2);
    }

}
