package org.drools.planner.examples.curriculumcourse.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Timeslot")
public class Timeslot extends AbstractPersistable implements Comparable<Timeslot> {

    private int timeslotIndex;

    public int getTimeslotIndex() {
        return timeslotIndex;
    }

    public void setTimeslotIndex(int timeslotIndex) {
        this.timeslotIndex = timeslotIndex;
    }

    public int compareTo(Timeslot other) {
        return new CompareToBuilder()
                .append(timeslotIndex, other.timeslotIndex)
                .toComparison();
    }

    @Override
    public String toString() {
        return Integer.toString(timeslotIndex);
    }

}