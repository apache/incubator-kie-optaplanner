package org.optaplanner.core.impl.testdata.domain.shadow;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public interface TestdataShadowIface  {

    public int getForwardSum();
    public int getValue();
    @InverseRelationShadowVariable(sourceVariableName = "previousEntity")
    public TestdataShadowEntity getNextEntity();
    public void setNextEntity(TestdataShadowEntity nextEntity);

}
