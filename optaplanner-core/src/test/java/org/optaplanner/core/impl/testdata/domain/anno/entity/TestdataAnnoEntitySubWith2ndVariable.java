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
public class TestdataAnnoEntitySubWith2ndVariable extends TestdataAnnoEntityParent {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        SimpleScore getScore();

        void setScore(SimpleScore score);

        @PlanningEntityProperty
        TestdataAnnoEntitySubWith2ndVariable getEntitySubWith2ndVariable();
    }

    public static SolutionDescriptor<Solution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(
                TestdataAnnoEntitySubWith2ndVariable.Solution.class, TestdataAnnoEntitySubWith2ndVariable.class);
    }

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubWith2ndVariable.class);
    }

    public static GenuineVariableDescriptor<Solution> buildVariableDescriptorForVar1() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("var1");
    }

    public static GenuineVariableDescriptor<Solution> buildVariableDescriptorForVar2() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("var2");
    }

    @PlanningVariable(valueRangeProviderRefs = "valrng2")
    private String var2;

    @ValueRangeProvider(id = "valrng2")
    public Collection<String> valueRange2() {
        return Collections.singleton("B");
    }

    public String getVar2() {
        return var2;
    }
}
