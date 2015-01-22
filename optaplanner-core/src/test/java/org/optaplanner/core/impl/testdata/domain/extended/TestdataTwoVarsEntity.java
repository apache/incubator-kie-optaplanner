package org.optaplanner.core.impl.testdata.domain.extended;

import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

public class TestdataTwoVarsEntity extends TestdataEntity {

    private TestdataValue secondValue;

    @PlanningVariable(valueRangeProviderRefs = "secondValueRange")
    public TestdataValue getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(TestdataValue value) {
        this.secondValue = value;
    }

}
