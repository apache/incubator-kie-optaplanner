package org.optaplanner.core.impl.testdata.score.director;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataEntityWithDifficultyComparator;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataSolutionWithDifficultyComparatorEntity;

public class TestdataComparableDifferentValuesCalculator implements EasyScoreCalculator<TestdataSolutionWithDifficultyComparatorEntity> {

    @Override
    public SimpleScore calculateScore(TestdataSolutionWithDifficultyComparatorEntity solution) {
        int score = 0;
        Map<TestdataValue, Integer> alreadyUsedValues = new HashMap<>();

        for (TestdataEntityWithDifficultyComparator entity : solution.getEntityList()) {
            if (entity.getValue() == null) {
                continue;
            }
            TestdataValue value = entity.getValue();
            if (alreadyUsedValues.containsKey(value)) {
                int incrementedValue = alreadyUsedValues.get(value) + 1;
                alreadyUsedValues.put(value, incrementedValue);
                score -= incrementedValue;
            } else {
                alreadyUsedValues.put(value, 1);
            }
        }
        return SimpleScore.of(score);
    }
}
