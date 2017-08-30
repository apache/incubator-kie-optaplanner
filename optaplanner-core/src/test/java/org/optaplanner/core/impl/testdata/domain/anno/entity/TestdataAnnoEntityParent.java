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
public class TestdataAnnoEntityParent {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        SimpleScore getScore();

        void setScore(SimpleScore score);

        @PlanningEntityProperty
        TestdataAnnoEntityParent getEntityParent();
    }

    public static SolutionDescriptor<Solution> buildParentSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(Solution.class, TestdataAnnoEntityParent.class);
    }

    public static EntityDescriptor<Solution> buildParentEntityDescriptor() {
        return buildParentSolutionDescriptor().findEntityDescriptorOrFail(TestdataAnnoEntityParent.class);
    }

    public static GenuineVariableDescriptor<Solution> buildParentVariableDescriptorForVar1() {
        return buildParentEntityDescriptor().getGenuineVariableDescriptor("var1");
    }

    @PlanningVariable(valueRangeProviderRefs = "valrng1")
    private String var1;

    @ValueRangeProvider(id = "valrng1")
    public Collection<String> valueRange1() {
        return Collections.singleton("A");
    }

    public String getVar1() {
        return var1;
    }
}
