package org.drools.solver.examples.travelingtournament.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Day extends AbstractPersistable implements Comparable<Day> {

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int compareTo(Day other) {
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
