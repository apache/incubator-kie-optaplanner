package org.optaplanner.core.impl.domain.variable.custom;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowAnchor;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowIface;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowSolution;

import java.util.List;

public class CustomShadowVariableIntegrationScoreFunction implements EasyScoreCalculator<TestdataShadowSolution> {

    @Override
    public Score calculateScore(TestdataShadowSolution solution) {
        List<TestdataShadowAnchor> startingPoints = solution.getAnchorList();
        int score = 0;
        for (TestdataShadowAnchor startingPoint : startingPoints) {
            TestdataShadowIface currentPoint = startingPoint;
            for (; currentPoint.getNextEntity() != null; ) {
                int diff = Math.abs(currentPoint.getValue() - currentPoint.getNextEntity().getValue());
                score += diff;
                currentPoint = currentPoint.getNextEntity();
            }
        }
        return SimpleScore.valueOf(-score);
    }
}
