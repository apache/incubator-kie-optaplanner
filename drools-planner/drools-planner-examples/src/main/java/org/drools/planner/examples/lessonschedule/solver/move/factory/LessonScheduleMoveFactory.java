package org.drools.planner.examples.lessonschedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.lessonschedule.domain.Lesson;
import org.drools.planner.examples.lessonschedule.domain.LessonSchedule;
import org.drools.planner.examples.lessonschedule.domain.Timeslot;
import org.drools.planner.examples.lessonschedule.solver.move.TimeslotChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class LessonScheduleMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        List<Move> moveList = new ArrayList<Move>();
        LessonSchedule lessonSchedule = (LessonSchedule) solution;
        for (Lesson lesson : lessonSchedule.getLessonList()) {
            for (Timeslot timeslot : lessonSchedule.getTimeslotList()) {
                moveList.add(new TimeslotChangeMove(lesson, timeslot));
            }
        }
        return moveList;
    }
    
}
