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
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.Shift;

/**
 * @author Geoffrey De Smet
 */
public class EmployeeChangeMove implements Move, TabuPropertyEnabled {

    private EmployeeAssignment employeeAssignment;
    private Employee toEmployee;

    public EmployeeChangeMove(EmployeeAssignment employeeAssignment, Employee toEmployee) {
        this.employeeAssignment = employeeAssignment;
        this.toEmployee = toEmployee;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(employeeAssignment.getEmployee(), toEmployee);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new EmployeeChangeMove(employeeAssignment, employeeAssignment.getEmployee());
    }

    public void doMove(WorkingMemory workingMemory) {
        NurseRosterMoveHelper.moveEmployee(workingMemory, employeeAssignment, toEmployee);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(employeeAssignment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeChangeMove) {
            EmployeeChangeMove other = (EmployeeChangeMove) o;
            return new EqualsBuilder()
                    .append(employeeAssignment, other.employeeAssignment)
                    .append(toEmployee, other.toEmployee)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employeeAssignment)
                .append(toEmployee)
                .toHashCode();
    }

    public String toString() {
        return employeeAssignment + " => " + toEmployee;
    }

}
