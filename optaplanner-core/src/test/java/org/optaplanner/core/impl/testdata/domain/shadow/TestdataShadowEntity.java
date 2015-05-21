package org.optaplanner.core.impl.testdata.domain.shadow;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.util.comparators.ShadowEntityComparator;

@PlanningEntity(difficultyComparatorClass = ShadowEntityComparator.class)
public class TestdataShadowEntity extends TestdataObject implements TestdataShadowIface {

    private int value;
    private TestdataShadowIface previousEntity;
    // inverse shadow variable
    private TestdataShadowEntity nextEntity;
    // shadow variable
    private int forwardSum;

    public static TestdataShadowEntity createNewShadowEntity(int value) {
        TestdataShadowEntity newOne = new TestdataShadowEntity();
        newOne.value = value;
        return newOne;
    }

    @Override
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED,
            valueRangeProviderRefs = {"entitiesList", "startingPointList"})
    public TestdataShadowIface getPreviousEntity() {
        return previousEntity;
    }

    public void setPreviousEntity(TestdataShadowIface previousEntity) {
        this.previousEntity = previousEntity;
    }

    @Override
    public TestdataShadowEntity getNextEntity() {
        return nextEntity;
    }

    @Override
    public void setNextEntity(TestdataShadowEntity nextEntity) {
        this.nextEntity = nextEntity;
    }

    @CustomShadowVariable(variableListenerClass = TestdataShadowVariableListener.class,
            sources = {@CustomShadowVariable.Source(variableName = "previousEntity")})
    @Override
    public int getForwardSum() {
        return forwardSum;
    }

    public void setForwardSum(int forwardSum) {
        this.forwardSum = forwardSum;
    }
}
