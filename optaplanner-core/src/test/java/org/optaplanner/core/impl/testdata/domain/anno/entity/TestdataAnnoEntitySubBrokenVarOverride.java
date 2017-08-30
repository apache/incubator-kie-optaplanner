package org.optaplanner.core.impl.testdata.domain.anno.entity;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;

@PlanningEntity
public class TestdataAnnoEntitySubBrokenVarOverride extends TestdataAnnoEntityParent {

    public static EntityDescriptor<Solution> buildEntityDescriptor() {
        return TestdataAnnoEntityParent.buildParentSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnnoEntitySubBrokenVarOverride.class);
    }

    @AnchorShadowVariable(sourceVariableName = "")
    @Override
    public String getVar1() {
        return super.getVar1();
    }
}
