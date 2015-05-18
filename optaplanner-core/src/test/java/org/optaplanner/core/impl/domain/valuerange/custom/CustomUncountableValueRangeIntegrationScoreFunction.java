package org.optaplanner.core.impl.domain.valuerange.custom;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataDoubleRangeSolution;

public class CustomUncountableValueRangeIntegrationScoreFunction implements EasyScoreCalculator<TestdataDoubleRangeSolution> {

    @Override
    public Score calculateScore(TestdataDoubleRangeSolution solution) {
        Double x = (solution.getEntities().get(0)).getValue();
        if ((x >= 0.0 && x <= 10.0) || (x >= 95.0 && x <= 105.0)) {
            return SimpleScore.valueOf(0);
        } else {
            return SimpleScore.valueOf(1);
        }
    }
}
