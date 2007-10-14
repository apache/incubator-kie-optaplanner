package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Exam extends AbstractPersistable implements Comparable<Exam> {

    private int duration;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int compareTo(Exam other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    public Exam clone() {
        Exam clone = new Exam();
        clone.id = id;
        clone.duration = duration;
        return clone;
    }

    public String toString() {
        return super.toString() + " " + duration + " + " + " @ ";
    }

}
