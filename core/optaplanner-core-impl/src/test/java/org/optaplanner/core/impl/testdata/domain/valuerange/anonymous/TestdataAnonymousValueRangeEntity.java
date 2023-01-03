package org.optaplanner.core.impl.testdata.domain.valuerange.anonymous;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataAnonymousValueRangeEntity extends TestdataObject {

    public static EntityDescriptor<TestdataAnonymousValueRangeSolution> buildEntityDescriptor() {
        return TestdataAnonymousValueRangeSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataAnonymousValueRangeEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataAnonymousValueRangeSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private Number numberValue;
    private Integer integerValue;
    private Long longValue;
    private BigInteger bigIntegerValue;
    private BigDecimal bigDecimalValue;
    private LocalDate localDateValue;
    private LocalTime localTimeValue;
    private LocalDateTime localDateTimeValue;
    private Year yearValue;

    public TestdataAnonymousValueRangeEntity() {
    }

    public TestdataAnonymousValueRangeEntity(String code) {
        super(code);
    }

    @PlanningVariable
    public Number getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Number numberValue) {
        this.numberValue = numberValue;
    }

    @PlanningVariable
    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    @PlanningVariable
    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    @PlanningVariable
    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    @PlanningVariable
    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    @PlanningVariable
    public LocalDate getLocalDateValue() {
        return localDateValue;
    }

    public void setLocalDateValue(LocalDate localDateValue) {
        this.localDateValue = localDateValue;
    }

    @PlanningVariable
    public LocalTime getLocalTimeValue() {
        return localTimeValue;
    }

    public void setLocalTimeValue(LocalTime localTimeValue) {
        this.localTimeValue = localTimeValue;
    }

    @PlanningVariable
    public LocalDateTime getLocalDateTimeValue() {
        return localDateTimeValue;
    }

    public void setLocalDateTimeValue(LocalDateTime localDateTimeValue) {
        this.localDateTimeValue = localDateTimeValue;
    }

    @PlanningVariable
    public Year getYearValue() {
        return yearValue;
    }

    public void setYearValue(Year yearValue) {
        this.yearValue = yearValue;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
