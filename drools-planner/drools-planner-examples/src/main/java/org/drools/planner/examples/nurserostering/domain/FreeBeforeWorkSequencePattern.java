package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("FreeBeforeWorkSequencePattern")
public class FreeBeforeWorkSequencePattern extends Pattern {

    private DayOfWeek firstWorkDayOfWeek; // null means any
    
    private ShiftType workShiftType; // null means any

    private int workDayLength;

    public DayOfWeek getFirstWorkDayOfWeek() {
        return firstWorkDayOfWeek;
    }

    public void setFirstWorkDayOfWeek(DayOfWeek firstWorkDayOfWeek) {
        this.firstWorkDayOfWeek = firstWorkDayOfWeek;
    }

    public ShiftType getWorkShiftType() {
        return workShiftType;
    }

    public void setWorkShiftType(ShiftType workShiftType) {
        this.workShiftType = workShiftType;
    }

    public int getWorkDayLength() {
        return workDayLength;
    }

    public void setWorkDayLength(int workDayLength) {
        this.workDayLength = workDayLength;
    }

    @Override
    public String toString() {
        return "Free followed by "  + workDayLength + " work days of " + workShiftType
                +" beginning on " + firstWorkDayOfWeek;
    }

}
