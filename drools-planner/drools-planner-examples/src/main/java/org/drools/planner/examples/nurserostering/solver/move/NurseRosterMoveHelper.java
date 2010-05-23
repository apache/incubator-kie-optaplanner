package org.drools.planner.examples.nurserostering.solver.move;


import org.drools.WorkingMemory;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.runtime.rule.FactHandle;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosterMoveHelper {

    public static void moveEmployee(WorkingMemory workingMemory, Assignment assignment, Employee toEmployee) {
        FactHandle factHandle = workingMemory.getFactHandle(assignment);
        assignment.setEmployee(toEmployee);
        workingMemory.update(factHandle, assignment);
    }

    private NurseRosterMoveHelper() {
    }

}
