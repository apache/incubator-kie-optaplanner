package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.ReinitializeVariableEntityRejectingFilter;

@PlanningEntity
public class TestdataCompositeCountableNullableFilteredEntity extends TestdataCompositeCountableNullableEntity {

    @Override
    @PlanningVariable(valueRangeProviderRefs = {"integerValue"}, nullable = true,
            reinitializeVariableEntityFilter = ReinitializeVariableEntityRejectingFilter.class)
    public Integer getValue() {
        return super.getValue();
    }

    @Override
    @ValueRangeProvider(id = "integerValue")
    public CountableValueRange getValueRange() {
        return super.getValueRange();
    }
}
