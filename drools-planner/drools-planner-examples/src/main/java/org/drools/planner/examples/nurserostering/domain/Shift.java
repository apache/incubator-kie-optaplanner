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
