package org.optaplanner.core.impl.testdata.domain.comparable;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity(difficultyComparatorClass = TestdataCodeComparator.class)
public class TestdataComparableEntity extends TestdataObject {

    public static EntityDescriptor<TestdataComparableSolution> buildEntityDescriptor() {
        SolutionDescriptor<TestdataComparableSolution> solutionDescriptor =
                TestdataComparableSolution.buildSolutionDescriptor();
        return solutionDescriptor.findEntityDescriptorOrFail(TestdataComparableEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataComparableSolution> buildVariableDescriptorForValue() {
        SolutionDescriptor<TestdataComparableSolution> solutionDescriptor =
                TestdataComparableSolution.buildSolutionDescriptor();
        EntityDescriptor<TestdataComparableSolution> entityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataComparableEntity.class);
        return entityDescriptor.getGenuineVariableDescriptor("value");
    }

    private TestdataComparableValue value;

    public TestdataComparableEntity() {
    }

    public TestdataComparableEntity(String code) {
        super(code);
    }

    public TestdataComparableEntity(String code, TestdataComparableValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = {"valueRange"},
            strengthComparatorClass = TestdataCodeComparator.class)
    public TestdataComparableValue getValue() {
        return value;
    }

    public void setValue(TestdataComparableValue value) {
        this.value = value;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
}
