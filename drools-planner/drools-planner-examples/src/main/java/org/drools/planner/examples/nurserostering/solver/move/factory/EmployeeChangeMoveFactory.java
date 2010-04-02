package org.drools.planner.examples.nurserostering.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.planner.examples.nurserostering.solver.move.EmployeeChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class EmployeeChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        NurseRoster nurseRoster = (NurseRoster) solution;
        List<Move> moveList = new ArrayList<Move>();
        List<Employee> employeeList = nurseRoster.getEmployeeList();
        for (EmployeeAssignment employeeAssignment : nurseRoster.getEmployeeAssignmentList()) {
            for (Employee employee : employeeList) {
                moveList.add(new EmployeeChangeMove(employeeAssignment, employee));
            }
        }
        return moveList;
    }

}
