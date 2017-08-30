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

@PlanningEntity
public class TestdataAnnoEntitySubBrokenVarRemoved extends TestdataAnnoEntityParent {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        SimpleScore getScore();

        void setScore(SimpleScore score);

        @PlanningEntityProperty
        TestdataAnnoEntitySubBrokenVarRemoved getEntity();
    }

    public static SolutionDescriptor<Solution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(Solution.class, TestdataAnnoEntitySubBrokenVarRemoved.class);
    }

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubBrokenVarRemoved.class);
    }

    @PlanningVariable(valueRangeProviderRefs = "valrng2")
    private String var2;

    @ValueRangeProvider(id = "valrng2")
    public Collection<String> valueRange2() {
        return Collections.singleton("V");
    }

    public String getVar2() {
        return var2;
    }

    // trying to remove a planning variable (unsupported)
    @Override
    public String getVar1() {
        return super.getVar1();
    }

}
