package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Shift")
public class Shift extends AbstractPersistable implements Comparable<Shift> {

    private ShiftDate shiftDate;
    private ShiftType shiftType;
    private int index;
    
    private int requiredEmployeeSize;

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRequiredEmployeeSize() {
        return requiredEmployeeSize;
    }

    public void setRequiredEmployeeSize(int requiredEmployeeSize) {
        this.requiredEmployeeSize = requiredEmployeeSize;
    }

    public int compareTo(Shift other) {
        return new CompareToBuilder()
                .append(shiftDate, other.shiftDate)
                .append(shiftType, other.shiftType)
                .toComparison();
    }

    @Override
    public String toString() {
        return shiftDate + "_" + shiftType;
    }

}
