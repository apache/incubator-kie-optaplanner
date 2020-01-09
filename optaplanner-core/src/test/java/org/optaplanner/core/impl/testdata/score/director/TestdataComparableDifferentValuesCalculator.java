package org.optaplanner.core.impl.testdata.score.director;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableSolution;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableValue;

public class TestdataComparableDifferentValuesCalculator implements EasyScoreCalculator<TestdataComparableSolution> {

    @Override
    public SimpleScore calculateScore(TestdataComparableSolution solution) {
        int score = 0;
        Map<TestdataComparableValue, Integer> alreadyUsedValues = new HashMap<>();

        for (TestdataComparableEntity entity : solution.getEntityList()) {
            if (entity.getValue() != null) {
                TestdataComparableValue value = entity.getValue();
                if (alreadyUsedValues.containsKey(value)) {
                    int incrementedValue = alreadyUsedValues.get(value) + 1;
                    alreadyUsedValues.put(value, incrementedValue);
                    score -= incrementedValue;
                } else {
                    alreadyUsedValues.put(value, 1);
                }
            }
        }
        return SimpleScore.of(score);
    }
}
