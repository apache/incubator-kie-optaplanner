package org.drools.planner.examples.nurserostering.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.solver.move.AssignmentSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class AssignmentSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        NurseRoster nurseRoster = (NurseRoster) solution;
        List<Assignment> assignmentList = nurseRoster.getAssignmentList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Assignment> leftIt = assignmentList.listIterator(); leftIt.hasNext();) {
            Assignment leftAssignment = leftIt.next();
            for (ListIterator<Assignment> rightIt = assignmentList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Assignment rightAssignment = rightIt.next();
                moveList.add(new AssignmentSwitchMove(leftAssignment, rightAssignment));
            }
        }
        return moveList;
    }

}
