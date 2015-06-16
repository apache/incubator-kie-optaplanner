package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

import java.util.Collection;
import java.util.List;

@PlanningSolution
public class TestdataIntegerRangeSolution extends TestdataObject implements Solution<SimpleScore> {

    private List<TestdataCompositeCountableEntity> entities;
    private SimpleScore score;

    @PlanningEntityCollectionProperty
    public List<TestdataCompositeCountableEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<TestdataCompositeCountableEntity> entities) {
        this.entities = entities;
    }

    @Override
    public SimpleScore getScore() {
        return score;
    }

    @Override
    public void setScore(SimpleScore score) {
        this.score = score;
    }

    @Override
    public Collection<?> getProblemFacts() {
        return null;
    }
}
