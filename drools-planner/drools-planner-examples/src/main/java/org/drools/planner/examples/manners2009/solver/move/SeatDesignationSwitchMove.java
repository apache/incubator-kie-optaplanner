package org.drools.planner.examples.manners2009.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.manners2009.domain.Seat;
import org.drools.planner.examples.manners2009.domain.SeatDesignation;

/**
 * @author Geoffrey De Smet
 */
public class SeatDesignationSwitchMove implements Move, TabuPropertyEnabled {

    private SeatDesignation leftSeatDesignation;
    private SeatDesignation rightSeatDesignation;

    public SeatDesignationSwitchMove(SeatDesignation leftSeatDesignation, SeatDesignation rightSeatDesignation) {
        this.leftSeatDesignation = leftSeatDesignation;
        this.rightSeatDesignation = rightSeatDesignation;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(leftSeatDesignation.getSeat(), rightSeatDesignation.getSeat());
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new SeatDesignationSwitchMove(rightSeatDesignation, leftSeatDesignation);
    }

    public void doMove(WorkingMemory workingMemory) {
        Seat oldLeftSeat = leftSeatDesignation.getSeat();
        Seat oldRightSeat = rightSeatDesignation.getSeat();
        moveSeat(workingMemory, leftSeatDesignation, oldRightSeat);
        moveSeat(workingMemory, rightSeatDesignation, oldLeftSeat);
    }

    // Extract to helper class if other moves are created
    private static void moveSeat(WorkingMemory workingMemory, SeatDesignation seatDesignation, Seat toSeat) {
        FactHandle factHandle = workingMemory.getFactHandle(seatDesignation);
        seatDesignation.setSeat(toSeat);
        workingMemory.update(factHandle, seatDesignation);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<SeatDesignation>asList(leftSeatDesignation, rightSeatDesignation);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SeatDesignationSwitchMove) {
            SeatDesignationSwitchMove other = (SeatDesignationSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftSeatDesignation, other.leftSeatDesignation)
                    .append(rightSeatDesignation, other.rightSeatDesignation)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftSeatDesignation)
                .append(rightSeatDesignation)
                .toHashCode();
    }

    public String toString() {
        return leftSeatDesignation + " <=> " + rightSeatDesignation;
    }

}