package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Period extends AbstractPersistable implements Comparable<Period> {

    private String startDateTimeString;
    private int dateInDays;
    private int startTimeInMinutes;
    private int durationInMinutes;
    private int penalty;

    public String getStartDateTimeString() {
        return startDateTimeString;
    }

    public void setStartDateTimeString(String startDateTimeString) {
        this.startDateTimeString = startDateTimeString;
    }

    public int getDateInDays() {
        return dateInDays;
    }

    public void setDateInDays(int dateInDays) {
        this.dateInDays = dateInDays;
    }

    public int getStartTimeInMinutes() {
        return startTimeInMinutes;
    }

    public void setStartTimeInMinutes(int startTimeInMinutes) {
        this.startTimeInMinutes = startTimeInMinutes;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public int compareTo(Period other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return super.toString() + " " + startDateTimeString + " {" + durationInMinutes + "}";
    }

}
