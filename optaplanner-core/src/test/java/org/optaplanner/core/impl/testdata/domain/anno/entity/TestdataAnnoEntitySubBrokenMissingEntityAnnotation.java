package org.optaplanner.core.impl.testdata.domain.anno.entity;

import org.optaplanner.core.api.domain.solution.PlanningEntityProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

//@PlanningEntity // missing
public class TestdataAnnoEntitySubBrokenMissingEntityAnnotation extends TestdataAnnoEntityParent {

    @PlanningSolution
    public static interface Solution {

        @PlanningScore
        SimpleScore getScore();

        void setScore(SimpleScore score);

        @PlanningEntityProperty
        TestdataAnnoEntitySubBrokenMissingEntityAnnotation getEntity();
    }

    public static SolutionDescriptor<Solution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(Solution.class, TestdataAnnoEntitySubBrokenMissingEntityAnnotation.class);
    }

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubBrokenMissingEntityAnnotation.class);
    }

    @PlanningVariable
    private String var1;

}
