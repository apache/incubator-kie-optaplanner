package org.optaplanner.core.impl.testdata.domain.shadow;

import org.optaplanner.core.impl.testdata.domain.TestdataObject;

public class TestdataShadowAnchor extends TestdataObject implements TestdataShadowIface {

    private int value; // good for easy determination where chain should start
    private TestdataShadowEntity nextEntity;

    public static TestdataShadowAnchor createNewShadowAnchor(int value) {
        TestdataShadowAnchor newOne = new TestdataShadowAnchor();
        newOne.value = value;
        return newOne;
    }

    @Override
    public TestdataShadowEntity getNextEntity() {
        return nextEntity;
    }

    @Override
    public void setNextEntity(TestdataShadowEntity nextEntity) {
        this.nextEntity = nextEntity;
    }

    @Override
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int getForwardSum() {
        return 0;
    }
}
