package org.optaplanner.core.impl.testdata.domain.comparable;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

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

    private TestdataValue value;

    public TestdataComparableEntity() {
    }

    public TestdataComparableEntity(String code) {
        super(code);
    }

    public TestdataComparableEntity(String code, TestdataValue value) {
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
