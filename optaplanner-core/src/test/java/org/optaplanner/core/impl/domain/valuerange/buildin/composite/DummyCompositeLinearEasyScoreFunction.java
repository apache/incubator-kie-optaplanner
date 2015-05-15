package org.optaplanner.core.impl.domain.valuerange.buildin.composite;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

public class DummyCompositeLinearEasyScoreFunction  implements EasyScoreCalculator<TestdataIntegerRangeSolution> {


    @Override
    public Score calculateScore(TestdataIntegerRangeSolution solution) {
        int x = (solution.getEntities().get(0)).getValue();
        if(x == 0 || x == 10 || x == 29 || (x >= 50 && x <= 55)) {
            x = 0;
        } else {
            x = 1;
        }
        return SimpleScore.valueOf(x);
    }
}
