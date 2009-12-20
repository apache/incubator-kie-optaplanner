package org.drools.planner.examples.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Examination;
import org.drools.planner.examples.examination.domain.Room;
import org.drools.planner.examples.examination.solver.move.RoomChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class RoomChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Room> roomList = examination.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Exam exam : examination.getExamList()) {
            for (Room room : roomList) {
                moveList.add(new RoomChangeMove(exam, room));
            }
        }
        return moveList;
    }

}