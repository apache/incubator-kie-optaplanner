package org.drools.planner.examples.nurserostering.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;

/**
 * @author Geoffrey De Smet
 */
public class AssignmentSwitchMove implements Move, TabuPropertyEnabled {

    private Assignment leftAssignment;
    private Assignment rightAssignment;

    public AssignmentSwitchMove(Assignment leftAssignment, Assignment rightAssignment) {
        this.leftAssignment = leftAssignment;
        this.rightAssignment = rightAssignment;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(leftAssignment.getEmployee(), rightAssignment.getEmployee());
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new AssignmentSwitchMove(rightAssignment, leftAssignment);
    }

    public void doMove(WorkingMemory workingMemory) {
        Employee oldLeftEmployee = leftAssignment.getEmployee();
        Employee oldRightEmployee = rightAssignment.getEmployee();
        NurseRosterMoveHelper.moveEmployee(workingMemory, leftAssignment, oldRightEmployee);
        NurseRosterMoveHelper.moveEmployee(workingMemory, rightAssignment, oldLeftEmployee);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<Assignment>asList(leftAssignment, rightAssignment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof AssignmentSwitchMove) {
            AssignmentSwitchMove other = (AssignmentSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftAssignment, other.leftAssignment)
                    .append(rightAssignment, other.rightAssignment)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftAssignment)
                .append(rightAssignment)
                .toHashCode();
    }

    public String toString() {
        return leftAssignment + " <=> " + rightAssignment;
    }

}
