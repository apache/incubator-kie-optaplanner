package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Exam extends AbstractPersistable implements Comparable<Exam> {

    private Topic topic;

    private Period period;
    private Room room;

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int compareTo(Exam other) {
        return new CompareToBuilder()
                .append(period, other.period)
                .append(room, other.room)
                .append(topic, other.topic)
                .toComparison();
    }

    public Exam clone() {
        Exam clone = new Exam();
        clone.id = id;
        clone.topic = topic;
        clone.period = period;
        clone.room = room;
        return clone;
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used
     * because the rule engine already requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Exam) {
            Exam other = (Exam) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(topic, other.topic)
                    .append(period, other.period)
                    .append(room, other.room)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used
     * because the rule engine already requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(topic)
                .append(period)
                .append(room)
                .toHashCode();
    }

    @Override
    public String toString() {
        return topic + " @ " + period + " in " + room;
    }

}
