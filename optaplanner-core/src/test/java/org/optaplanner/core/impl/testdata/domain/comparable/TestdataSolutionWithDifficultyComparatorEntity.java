package org.optaplanner.core.impl.testdata.domain.comparable;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningSolution
public class TestdataSolutionWithDifficultyComparatorEntity extends TestdataObject {

    public static SolutionDescriptor<TestdataSolutionWithDifficultyComparatorEntity> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataSolutionWithDifficultyComparatorEntity.class, TestdataEntityWithDifficultyComparator.class);
    }

    private List<TestdataValue> valueList;
    private List<TestdataEntityWithDifficultyComparator> entityList;

    private SimpleScore score;

    public TestdataSolutionWithDifficultyComparatorEntity() {
    }

    public TestdataSolutionWithDifficultyComparatorEntity(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    public List<TestdataValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<TestdataValue> valueList) {
        this.valueList = valueList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataEntityWithDifficultyComparator> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataEntityWithDifficultyComparator> entityList) {
        this.entityList = entityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
}
