package org.drools.solver.examples.lessonschedule.solver;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.decider.selector.CachedMoveListMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.lessonschedule.domain.Lesson;
import org.drools.solver.examples.lessonschedule.domain.LessonSchedule;
import org.drools.solver.examples.lessonschedule.domain.Timeslot;

/**
 * @author Geoffrey De Smet
 */
public class LessonScheduleMoveFactory extends CachedMoveListMoveFactory {

    public List<Move> createMoveList(Solution solution) {
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
