package org.drools.planner.examples.nurserostering.domain;

import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("ShiftDate")
public class ShiftDate extends AbstractPersistable implements Comparable<ShiftDate> {

    private int dayIndex;
    private String dateString;
    private DayOfWeek dayOfWeek;

    private List<Shift> shiftList;

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    public String getLabel() {
        return dateString.substring(5);
    }

    public int compareTo(ShiftDate other) {
        return new CompareToBuilder()
                .append(dayIndex, other.dayIndex)
                .toComparison();
    }

    @Override
    public String toString() {
        return dateString + "(" + dayOfWeek + ")";
    }

}
