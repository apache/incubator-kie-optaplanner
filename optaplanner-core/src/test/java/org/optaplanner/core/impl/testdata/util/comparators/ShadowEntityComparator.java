package org.optaplanner.core.impl.testdata.util.comparators;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowEntity;

import java.util.Comparator;

public class ShadowEntityComparator implements Comparator<TestdataShadowEntity> {

    @Override
    public int compare(TestdataShadowEntity t, TestdataShadowEntity t1) {
        return new CompareToBuilder().append(t1.getValue(), t.getValue()).toComparison();
    }
}
