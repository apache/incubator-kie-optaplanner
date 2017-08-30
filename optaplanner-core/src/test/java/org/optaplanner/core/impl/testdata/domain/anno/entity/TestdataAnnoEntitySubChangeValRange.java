package org.optaplanner.core.impl.testdata.domain.anno.entity;

import java.util.Collection;
import java.util.Collections;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningEntityProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;

@PlanningEntity
public class TestdataAnnoEntitySubChangeValRange extends TestdataAnnoEntityParent {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        default SimpleScore getScore() {
            return null;
        }

        default void setScore(SimpleScore score) {
        }

        @PlanningEntityProperty
        default TestdataAnnoEntitySubChangeValRange getEntitySubWith2ndVariable() {
            return null;
        }
    }

    public static SolutionDescriptor<Solution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(
                TestdataAnnoEntitySubChangeValRange.Solution.class, TestdataAnnoEntitySubChangeValRange.class);
    }

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubChangeValRange.class);
    }

    public static GenuineVariableDescriptor<Solution> buildVariableDescriptorForVar1() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("var1");
    }

    @ValueRangeProvider(id = "valrng2")
    public Collection<String> valueRange2() {
        return Collections.singleton("B");
    }

    @PlanningVariable(valueRangeProviderRefs = "valrng2")
    private String var1;

    @Override
    public String getVar1() {
        return var1;
    }
}
