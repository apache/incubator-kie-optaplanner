package org.optaplanner.core.impl.heuristic.selector.common.decorator;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

public class ReinitializeVariableEntityFilterIntegrationScoreFunction implements EasyScoreCalculator<TestdataIntegerRangeSolution> {

    @Override
    public Score calculateScore(TestdataIntegerRangeSolution solution) {
        Integer x = (solution.getEntities().get(0)).getValue();
        if(x == null) {
            return SimpleScore.valueOf(Integer.MIN_VALUE);
        }
        return SimpleScore.valueOf(x);
    }

}
