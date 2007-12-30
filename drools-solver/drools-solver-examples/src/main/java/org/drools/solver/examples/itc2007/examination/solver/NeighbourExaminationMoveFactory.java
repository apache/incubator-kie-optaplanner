package org.drools.solver.examples.itc2007.examination.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.solver.core.localsearch.decider.selector.AbstractMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class NeighbourExaminationMoveFactory extends AbstractMoveFactory {

    public Iterator<Move> iterator() {
        List<Move> moveList = new ArrayList<Move>();
        Examination examination = (Examination) localSearchSolver.getCurrentSolution();
        List<Period> periodList = examination.getPeriodList();
        // periodList should not be empty
        int firstPeriodDay = periodList.get(0).getDateInDays();
        int lastPeriodDay = periodList.get(periodList.size() - 1).getDateInDays();
        List<Room> roomList = examination.getRoomList();
        long firstRoomId = roomList.get(0).getId();
        long lastRoomId = roomList.get(roomList.size() - 1).getId();
        for (Exam exam : examination.getExamList()) {
            for (Period period : periodList) {
                if ((Math.abs(period.getDateInDays() - exam.getPeriod().getDateInDays()) <= 1)
                        || (period.getDateInDays() == firstPeriodDay
                            && exam.getPeriod().getDateInDays() == lastPeriodDay)
                        || (period.getDateInDays() == lastPeriodDay
                            && exam.getPeriod().getDateInDays() == firstPeriodDay)
                        ) {
                    moveList.add(new PeriodChangeMove(exam, period));
                }
            }
            for (Room room : roomList) {
                if ((Math.abs(room.getId() - exam.getRoom().getId()) <= 1)
                        || (room.getId() == firstRoomId
                            && exam.getRoom().getId() == lastRoomId)
                        || (room.getId() == lastRoomId
                            && exam.getRoom().getId() == firstRoomId)
                        ) {
                    moveList.add(new RoomChangeMove(exam, room));
                }
            }
        }
        return moveList.iterator();
    }

}