package org.optaplanner.core.impl.testdata.domain.comparable;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity(difficultyComparatorClass = TestdataCodeComparator.class)
public class TestdataEntityWithDifficultyComparator extends TestdataObject {

    public static EntityDescriptor<TestdataSolutionWithDifficultyComparatorEntity> buildEntityDescriptor() {
        SolutionDescriptor<TestdataSolutionWithDifficultyComparatorEntity> solutionDescriptor =
                TestdataSolutionWithDifficultyComparatorEntity.buildSolutionDescriptor();
        return solutionDescriptor.findEntityDescriptorOrFail(TestdataEntityWithDifficultyComparator.class);
    }

    public static GenuineVariableDescriptor<TestdataSolutionWithDifficultyComparatorEntity> buildVariableDescriptorForValue() {
        SolutionDescriptor<TestdataSolutionWithDifficultyComparatorEntity> solutionDescriptor =
                TestdataSolutionWithDifficultyComparatorEntity.buildSolutionDescriptor();
        EntityDescriptor<TestdataSolutionWithDifficultyComparatorEntity> entityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataEntityWithDifficultyComparator.class);
        return entityDescriptor.getGenuineVariableDescriptor("value");
    }

    private TestdataValue value;

    public TestdataEntityWithDifficultyComparator() {
    }

    public TestdataEntityWithDifficultyComparator(String code) {
        super(code);
    }

    public TestdataEntityWithDifficultyComparator(String code, TestdataValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = {"valueRange"},
            strengthComparatorClass = TestdataCodeComparator.class)
    public TestdataValue getValue() {
        return value;
    }

    public void setValue(TestdataValue value) {
        this.value = value;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
}
