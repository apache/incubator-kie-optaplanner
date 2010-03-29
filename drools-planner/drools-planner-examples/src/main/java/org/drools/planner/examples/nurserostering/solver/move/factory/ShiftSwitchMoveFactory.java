package org.drools.planner.examples.nurserostering.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.solver.move.ShiftSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class ShiftSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        NurseRoster nurseRoster = (NurseRoster) solution;
        List<EmployeeAssignment> employeeAssignmentList = nurseRoster.getEmployeeAssignmentList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<EmployeeAssignment> leftIt = employeeAssignmentList.listIterator(); leftIt.hasNext();) {
            EmployeeAssignment leftEmployeeAssignment = leftIt.next();
            for (ListIterator<EmployeeAssignment> rightIt = employeeAssignmentList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                EmployeeAssignment rightEmployeeAssignment = rightIt.next();
                if (leftEmployeeAssignment.getShiftDate().equals(rightEmployeeAssignment.getShiftDate())) {
                    moveList.add(new ShiftSwitchMove(leftEmployeeAssignment, rightEmployeeAssignment));
                }
            }
        }
        return moveList;
    }

}
