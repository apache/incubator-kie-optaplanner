package org.optaplanner.core.impl.domain.valuerange.buildin.composite;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

public class NullableCountableValueRangeIntegrationScoreFunction implements EasyScoreCalculator<TestdataIntegerRangeSolution> {

    @Override
    public Score calculateScore(TestdataIntegerRangeSolution solution) {
        Integer x = (solution.getEntities().get(0)).getValue();
        if(x == null) {
            return SimpleScore.valueOf(1);  // force null value
        }
        if(x >= 0  && x <= 10) {
            return SimpleScore.valueOf(0);
        } else {
            return SimpleScore.valueOf(2);
        }
    }

}
