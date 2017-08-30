package org.optaplanner.core.impl.testdata.domain.anno.entity;

import java.util.Collection;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningEntityProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

@PlanningEntity
public class TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        SimpleScore getScore();

        void setScore(SimpleScore score);

        @PlanningEntityProperty
        TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride getEntity();
    }

    public static SolutionDescriptor<Solution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(
                Solution.class, TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride.class);
    }

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride.class);
    }

    @PlanningVariable(valueRangeProviderRefs = "valrng1")
    private String var1;

    @PlanningVariable(valueRangeProviderRefs = "valrng1")
    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    @ValueRangeProvider(id = "valrng1")
    public Collection<String> valueRange() {
        return null;
    }
}
