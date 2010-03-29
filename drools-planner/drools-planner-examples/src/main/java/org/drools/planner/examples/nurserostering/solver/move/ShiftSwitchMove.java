package org.drools.planner.examples.nurserostering.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.Shift;

/**
 * @author Geoffrey De Smet
 */
public class ShiftSwitchMove implements Move, TabuPropertyEnabled {

    private EmployeeAssignment leftEmployeeAssignment;
    private EmployeeAssignment rightEmployeeAssignment;

    public ShiftSwitchMove(EmployeeAssignment leftEmployeeAssignment, EmployeeAssignment rightEmployeeAssignment) {
        this.leftEmployeeAssignment = leftEmployeeAssignment;
        this.rightEmployeeAssignment = rightEmployeeAssignment;
        if (!leftEmployeeAssignment.getShiftDate().equals(rightEmployeeAssignment.getShiftDate())) {
            throw new IllegalArgumentException("The leftEmployeeAssignment (" + leftEmployeeAssignment
                    + ") must have the same shiftDate as the rightEmployeeAssignment ("
                    + rightEmployeeAssignment + ").");
        }
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(leftEmployeeAssignment.getShift(), rightEmployeeAssignment.getShift());
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new ShiftSwitchMove(rightEmployeeAssignment, leftEmployeeAssignment);
    }

    public void doMove(WorkingMemory workingMemory) {
        Shift oldLeftShift = leftEmployeeAssignment.getShift();
        Shift oldRightShift = rightEmployeeAssignment.getShift();
        NurseRosterMoveHelper.moveShift(workingMemory, leftEmployeeAssignment, oldRightShift);
        NurseRosterMoveHelper.moveShift(workingMemory, rightEmployeeAssignment, oldLeftShift);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<EmployeeAssignment>asList(leftEmployeeAssignment, rightEmployeeAssignment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ShiftSwitchMove) {
            ShiftSwitchMove other = (ShiftSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftEmployeeAssignment, other.leftEmployeeAssignment)
                    .append(rightEmployeeAssignment, other.rightEmployeeAssignment)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftEmployeeAssignment)
                .append(rightEmployeeAssignment)
                .toHashCode();
    }

    public String toString() {
        return leftEmployeeAssignment + " <=> " + rightEmployeeAssignment;
    }

}
