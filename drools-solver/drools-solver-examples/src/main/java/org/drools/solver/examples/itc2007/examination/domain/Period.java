package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Period extends AbstractPersistable implements Comparable<Period> {

    private String startDateTimeString;
    private int periodIndex;
    private int dayIndex;
    private int duration; // in minutes
    private int penalty;
    private boolean frontLoadLast;

    public String getStartDateTimeString() {
        return startDateTimeString;
    }

    public void setStartDateTimeString(String startDateTimeString) {
        this.startDateTimeString = startDateTimeString;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public boolean isFrontLoadLast() {
        return frontLoadLast;
    }

    public void setFrontLoadLast(boolean frontLoadLast) {
        this.frontLoadLast = frontLoadLast;
    }

    public int compareTo(Period other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return id + "_d" + duration;
    }

}
