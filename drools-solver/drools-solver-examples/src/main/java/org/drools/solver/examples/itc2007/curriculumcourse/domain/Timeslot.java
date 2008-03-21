package org.drools.solver.examples.itc2007.curriculumcourse.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
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