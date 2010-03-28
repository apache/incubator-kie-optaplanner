package org.drools.planner.examples.nurserostering.solver.move;


import org.drools.WorkingMemory;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.runtime.rule.FactHandle;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosterMoveHelper {

    public static void moveShift(WorkingMemory workingMemory, EmployeeAssignment employeeAssignment, Shift shift) {
        FactHandle factHandle = workingMemory.getFactHandle(employeeAssignment);
        employeeAssignment.setShift(shift);
        workingMemory.update(factHandle, employeeAssignment);
    }

    private NurseRosterMoveHelper() {
    }

}
