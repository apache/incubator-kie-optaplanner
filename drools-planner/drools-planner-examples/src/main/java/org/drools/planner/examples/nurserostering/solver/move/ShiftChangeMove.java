package org.drools.planner.examples.nurserostering.solver.move;

import java.util.Collection;
import java.util.Collections;

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
public class ShiftChangeMove implements Move, TabuPropertyEnabled {

    private EmployeeAssignment employeeAssignment;
    private Shift toShift;

    public ShiftChangeMove(EmployeeAssignment employeeAssignment, Shift toShift) {
        this.employeeAssignment = employeeAssignment;
        this.toShift = toShift;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(employeeAssignment.getShift(), toShift);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new ShiftChangeMove(employeeAssignment, employeeAssignment.getShift());
    }

    public void doMove(WorkingMemory workingMemory) {
        NurseRosterMoveHelper.moveShift(workingMemory, employeeAssignment, toShift);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(employeeAssignment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ShiftChangeMove) {
            ShiftChangeMove other = (ShiftChangeMove) o;
            return new EqualsBuilder()
                    .append(employeeAssignment, other.employeeAssignment)
                    .append(toShift, other.toShift)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employeeAssignment)
                .append(toShift)
                .toHashCode();
    }

    public String toString() {
        return employeeAssignment + " => " + toShift;
    }

}
