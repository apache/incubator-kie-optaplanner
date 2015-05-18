package org.optaplanner.core.impl.testdata.domain.valuerange;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataUncountableEntity extends TestdataObject {

    private ValueRange valueRange;
    private double value;

    @PlanningVariable(valueRangeProviderRefs = {"doubleValue"})
    public double getValue() {
        return value;
    }

    @ValueRangeProvider(id = "doubleValue")
    public ValueRange getValueRange() {
        return valueRange;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValueRange(ValueRange valueRange) {
        this.valueRange = valueRange;
    }

}
