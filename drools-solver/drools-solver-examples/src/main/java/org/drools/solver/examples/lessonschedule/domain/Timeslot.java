package org.drools.solver.examples.lessonschedule.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Timeslot extends AbstractPersistable implements Comparable<Timeslot> {

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int compareTo(Timeslot other) {
        return new CompareToBuilder()
                .append(index, other.index)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

}
