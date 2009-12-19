package org.drools.planner.examples.curriculumcourse.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Period")
public class Period extends AbstractPersistable implements Comparable<Period> {

    private Day day;
    private Timeslot timeslot;

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public int compareTo(Period other) {
        return new CompareToBuilder()
                .append(day, other.day)
                .append(timeslot, other.timeslot)
                .toComparison();
    }

    @Override
    public String toString() {
        return day + "-" + timeslot;
    }

}