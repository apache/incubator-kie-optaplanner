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

    private static final int PERIOD_JUMP = 2;
    private static final int ROOM_JUMP = 2;

    public Iterator<Move> iterator() {
        List<Move> moveList = new ArrayList<Move>();
        Examination examination = (Examination) localSearchSolver.getCurrentSolution();
        List<Period> periodList = examination.getPeriodList();
        // periodList should not be empty
        int firstPeriodIndex = periodList.get(0).getPeriodIndex();
        int lastPeriodIndex = periodList.get(periodList.size() - PERIOD_JUMP).getPeriodIndex();
        List<Room> roomList = examination.getRoomList();
        long firstRoomId = roomList.get(0).getId();
        long lastRoomId = roomList.get(roomList.size() - PERIOD_JUMP).getId();
        for (Exam exam : examination.getExamList()) {
            for (Period period : periodList) {
                if ((Math.abs(period.getPeriodIndex() - exam.getPeriod().getPeriodIndex()) <= PERIOD_JUMP)
                        || (period.getPeriodIndex() == firstPeriodIndex
                            && exam.getPeriod().getPeriodIndex() == lastPeriodIndex)
                        || (period.getPeriodIndex() == lastPeriodIndex
                            && exam.getPeriod().getPeriodIndex() == firstPeriodIndex)
                        ) {
                    moveList.add(new PeriodChangeMove(exam, period));
                }
            }
            for (Room room : roomList) {
                if ((Math.abs(room.getId() - exam.getRoom().getId()) <= ROOM_JUMP)
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
        // TODO re-enable this stuff as it's a lot faster
//        int maximumMoveSize = 100;
//        // TODO not fair for first and last moves in move list
//        int randomStart = localSearchSolver.getRandom().nextInt(moveList.size() - maximumMoveSize);
//        return moveList.subList(randomStart, randomStart + maximumMoveSize).iterator();
    }

}