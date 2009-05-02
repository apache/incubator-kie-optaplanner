package org.drools.solver.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class SeatDesignation extends AbstractPersistable implements Comparable<SeatDesignation> {

    private Guest guest;
    private Seat seat;

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public int compareTo(SeatDesignation other) {
        return new CompareToBuilder()
                .append(guest, other.guest)
                .append(seat, other.seat)
                .append(id, other.id)
                .toComparison();
    }

    public SeatDesignation clone() {
        SeatDesignation clone = new SeatDesignation();
        clone.id = id;
        clone.guest = guest;
        clone.seat = seat;
        return clone;
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SeatDesignation) {
            SeatDesignation other = (SeatDesignation) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(guest, other.guest)
                    .append(seat, other.seat)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(guest)
                .append(seat)
                .toHashCode();
    }

    @Override
    public String toString() {
        return guest + " @ " + seat;
    }

    public Job getGuestJob() {
        return getGuest().getJob();
    }

    public Table getSeatTable() {
        return getSeat().getTable();
    }

}