package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TestdataCompositeCountableNullableEntity extends TestdataCompositeCountableEntity {

    @Override
    @PlanningVariable(valueRangeProviderRefs = {"integerValue"}, nullable = true)
    public Integer getValue() {
        return super.getValue();
    }

    @Override
    @ValueRangeProvider(id = "integerValue")
    public CountableValueRange getValueRange() {
        return super.getValueRange();
    }

}
