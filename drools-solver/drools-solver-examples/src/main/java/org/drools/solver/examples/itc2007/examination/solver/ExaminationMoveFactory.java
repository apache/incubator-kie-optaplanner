package org.drools.solver.examples.itc2007.examination.solver;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.decider.selector.CachedMoveListMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.solver.move.PeriodChangeBulkMove;
import org.drools.solver.examples.itc2007.examination.solver.move.PeriodChangeMove;
import org.drools.solver.examples.itc2007.examination.solver.move.RoomChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationMoveFactory extends CachedMoveListMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Exam exam : examination.getExamList()) {
            if (exam.getExamCoincidence() != null) {
                if (exam.isCoincidenceLeader()) {
                    for (Period period : periodList) {
                        moveList.add(new PeriodChangeBulkMove(
                                exam.getExamCoincidence().getCoincidenceExamSet(), period));
                    }
                }
            } else {
                for (Period period : periodList) {
                    moveList.add(new PeriodChangeMove(exam, period));
                }
            }
            for (Room room : roomList) {
                moveList.add(new RoomChangeMove(exam, room));
            }
        }
        return moveList;
    }

}