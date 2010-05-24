package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.contract.Contract;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("RosterInfo")
public class RosterInfo extends AbstractPersistable implements Comparable<RosterInfo> {

    private ShiftDate firstShiftDate;
    private ShiftDate lastShiftDate;

    public RosterInfo(ShiftDate firstShiftDate, ShiftDate lastShiftDate) {
        this.firstShiftDate = firstShiftDate;
        this.lastShiftDate = lastShiftDate;
    }

    public ShiftDate getFirstShiftDate() {
        return firstShiftDate;
    }

    public void setFirstShiftDate(ShiftDate firstShiftDate) {
        this.firstShiftDate = firstShiftDate;
    }

    public ShiftDate getLastShiftDate() {
        return lastShiftDate;
    }

    public void setLastShiftDate(ShiftDate lastShiftDate) {
        this.lastShiftDate = lastShiftDate;
    }

    public int compareTo(RosterInfo other) {
        return new CompareToBuilder()
                .append(firstShiftDate, other.firstShiftDate)
                .append(lastShiftDate, other.lastShiftDate)
                .toComparison();
    }

    @Override
    public String toString() {
        return firstShiftDate + " - " + lastShiftDate;
    }

    public int getFirstShiftDateDayIndex() {
        return firstShiftDate.getDayIndex();
    }

    public int getLastShiftDateDayIndex() {
        return lastShiftDate.getDayIndex();
    }

}
