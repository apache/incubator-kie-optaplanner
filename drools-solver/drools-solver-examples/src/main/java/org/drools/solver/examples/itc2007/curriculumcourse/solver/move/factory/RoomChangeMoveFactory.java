package org.drools.solver.examples.itc2007.curriculumcourse.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Room;
import org.drools.solver.examples.itc2007.curriculumcourse.solver.move.RoomChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class RoomChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) solution;
        List<Room> roomList = schedule.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Lecture lecture : schedule.getLectureList()) {
            for (Room room : roomList) {
                moveList.add(new RoomChangeMove(lecture, room));
            }
        }
        return moveList;
    }

}