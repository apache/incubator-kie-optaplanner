package org.optaplanner.core.impl.testdata.score.director;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataDifficultyComparingEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataDifficultyComparingSolution;

/**
 * This class calculates the score of a solution by penalizing repeated value occurrences held by entities.
 *
 * - If yes: put it into the map as a key and set it's value to 1.
 * - If no: increment the value and deduce the score by it.
 *
 *  The higher the number of occurrences for a given value, the higher the penalty to the score.
 *
 *  This behavior is presented in the example below:
 *  4 entities, 3 of which having the same value
 *  entity1 value1 -> id of value1 put into the map with key: value1.getId() and map-value:1 (score == 0),
 *  entity2 value1 -> found value1 in the map, decrement the map-value by 1 (to 2), deduce the score by it (score == -2),
 *  entity3 value2 -> id of value2 put into the map with key: value2.getId() and map-value:1 as the value (score == -2),
 *  entity4 value1 -> found value1 in the map, decrement the map-value by 1 (to 3), deduce the score by it (score == -5),
 *  resulting in a final score: -5
 */
public class TestdataComparableDifferentValuesCalculator implements EasyScoreCalculator<TestdataDifficultyComparingSolution> {

    @Override
    public SimpleScore calculateScore(TestdataDifficultyComparingSolution solution) {
        int score = 0;
        Map<TestdataValue, Integer> alreadyUsedValues = new HashMap<>();

        for (TestdataDifficultyComparingEntity entity : solution.getEntityList()) {
            if (entity.getValue() == null) {
                continue;
            }
            TestdataValue value = entity.getValue();
            if (alreadyUsedValues.containsKey(value)) {
                int incrementedValue = 1;
//                int incrementedValue = alreadyUsedValues.get(value) + 1;
                alreadyUsedValues.put(value, incrementedValue);
                score -= incrementedValue;
            } else {
                alreadyUsedValues.put(value, 1);
            }
        }
        return SimpleScore.of(score);
    }
}
