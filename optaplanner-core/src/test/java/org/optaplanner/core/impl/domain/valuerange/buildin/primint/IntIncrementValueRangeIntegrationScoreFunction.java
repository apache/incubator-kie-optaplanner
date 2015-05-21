package org.optaplanner.core.impl.domain.valuerange.buildin.primint;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

public class IntIncrementValueRangeIntegrationScoreFunction implements EasyScoreCalculator<TestdataIntegerRangeSolution> {

    @Override
    public Score calculateScore(TestdataIntegerRangeSolution solution) {
        Integer x = (solution.getEntities().get(0)).getValue();
        if (x % 100 == 0 && (x < 0 || x > 10000)) {
            x = 0;
        } else {
            x = 1;
        }
        return SimpleScore.valueOf(x);
    }
}
