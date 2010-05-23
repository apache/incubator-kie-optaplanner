package org.drools.planner.examples.nurserostering.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.Assignment;

/**
 * @author Geoffrey De Smet
 */
public class EmployeeChangeMove implements Move, TabuPropertyEnabled {

    private Assignment assignment;
    private Employee toEmployee;

    public EmployeeChangeMove(Assignment assignment, Employee toEmployee) {
        this.assignment = assignment;
        this.toEmployee = toEmployee;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(assignment.getEmployee(), toEmployee);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new EmployeeChangeMove(assignment, assignment.getEmployee());
    }

    public void doMove(WorkingMemory workingMemory) {
        NurseRosterMoveHelper.moveEmployee(workingMemory, assignment, toEmployee);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(assignment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeChangeMove) {
            EmployeeChangeMove other = (EmployeeChangeMove) o;
            return new EqualsBuilder()
                    .append(assignment, other.assignment)
                    .append(toEmployee, other.toEmployee)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(assignment)
                .append(toEmployee)
                .toHashCode();
    }

    public String toString() {
        return assignment + " => " + toEmployee;
    }

}
