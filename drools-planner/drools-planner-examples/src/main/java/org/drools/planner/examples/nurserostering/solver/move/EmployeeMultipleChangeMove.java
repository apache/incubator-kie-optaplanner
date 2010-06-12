package org.drools.planner.examples.nurserostering.solver.move;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
public class EmployeeMultipleChangeMove implements Move, TabuPropertyEnabled {

    private Employee fromEmployee;
    private List<Assignment> assignmentList;
    private Employee toEmployee;

    public EmployeeMultipleChangeMove(Employee fromEmployee, List<Assignment> assignmentList, Employee toEmployee) {
        this.fromEmployee = fromEmployee;
        this.assignmentList = assignmentList;
        this.toEmployee = toEmployee;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(fromEmployee, toEmployee);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new EmployeeMultipleChangeMove(toEmployee, assignmentList, fromEmployee);
    }

    public void doMove(WorkingMemory workingMemory) {
        for (Assignment assignment : assignmentList) {
            if (!assignment.getEmployee().equals(fromEmployee)) {
                throw new IllegalStateException("The assignment (" + assignment + ") should have the same employee ("
                        + fromEmployee + ") as the fromEmployee (" + fromEmployee + ").");
            }
            NurseRosterMoveHelper.moveEmployee(workingMemory, assignment, toEmployee);
        }
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(assignmentList);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeMultipleChangeMove) {
            EmployeeMultipleChangeMove other = (EmployeeMultipleChangeMove) o;
            return new EqualsBuilder()
                    .append(fromEmployee, other.fromEmployee)
                    .append(assignmentList, other.assignmentList)
                    .append(toEmployee, other.toEmployee)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(fromEmployee)
                .append(assignmentList)
                .append(toEmployee)
                .toHashCode();
    }

    public String toString() {
        return assignmentList + " => " + toEmployee;
    }

}
