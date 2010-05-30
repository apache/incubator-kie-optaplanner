package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("WorkBeforeFreeSequencePattern")
public class WorkBeforeFreeSequencePattern extends Pattern {

    private DayOfWeek workDayOfWeek; // null means any
    private ShiftType workShiftType; // null means any

    private int freeDayLength;

    public DayOfWeek getWorkDayOfWeek() {
        return workDayOfWeek;
    }

    public void setWorkDayOfWeek(DayOfWeek workDayOfWeek) {
        this.workDayOfWeek = workDayOfWeek;
    }

    public ShiftType getWorkShiftType() {
        return workShiftType;
    }

    public void setWorkShiftType(ShiftType workShiftType) {
        this.workShiftType = workShiftType;
    }

    public int getFreeDayLength() {
        return freeDayLength;
    }

    public void setFreeDayLength(int freeDayLength) {
        this.freeDayLength = freeDayLength;
    }

    @Override
    public String toString() {
        return "Work " + workShiftType + " on " + workDayOfWeek + " followed by " + freeDayLength + " free days";
    }

}
