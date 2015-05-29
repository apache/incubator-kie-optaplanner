package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataCompositeCountableEntity extends TestdataObject {

    private CountableValueRange valueRange;
    private Integer value;

    @PlanningVariable(valueRangeProviderRefs = {"integerValue"})
    public Integer getValue() {
        return value;
    }

    @ValueRangeProvider(id = "integerValue")
    public CountableValueRange getValueRange() {
        return valueRange;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setValueRange(CountableValueRange valueRange) {
        this.valueRange = valueRange;
    }

}
