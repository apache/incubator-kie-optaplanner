package org.drools.planner.examples.nurserostering.solver.move;


import org.drools.WorkingMemory;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.runtime.rule.FactHandle;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosterMoveHelper {

    public static void moveEmployee(WorkingMemory workingMemory, EmployeeAssignment employeeAssignment, Employee toEmployee) {
        FactHandle factHandle = workingMemory.getFactHandle(employeeAssignment);
        employeeAssignment.setEmployee(toEmployee);
        workingMemory.update(factHandle, employeeAssignment);
    }

    private NurseRosterMoveHelper() {
    }

}
