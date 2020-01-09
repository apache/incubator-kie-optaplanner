package org.optaplanner.core.impl.testdata.domain.comparable;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

public class TestdataCodeComparator implements Comparator<TestdataObject> {

    public int compare(TestdataObject a, TestdataObject b) {
        return new CompareToBuilder()
                .append(a.getCode(), b.getCode())
                .toComparison();
    }
}