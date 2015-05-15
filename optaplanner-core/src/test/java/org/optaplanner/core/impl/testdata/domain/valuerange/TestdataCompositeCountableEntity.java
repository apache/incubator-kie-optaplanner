package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.valuerange.buildin.composite.CompositeCountableValueRange;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataCompositeCountableEntity extends TestdataObject {

    private CompositeCountableValueRange valueRange;
    private int value;

    @PlanningVariable(valueRangeProviderRefs = {"integerValue"})
    public int getValue() {
        return value;
    }

    @ValueRangeProvider(id = "integerValue")
    public CompositeCountableValueRange getValueRange() {
        return valueRange;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setValueRange(CompositeCountableValueRange valueRange) {
        this.valueRange = valueRange;
    }

}
