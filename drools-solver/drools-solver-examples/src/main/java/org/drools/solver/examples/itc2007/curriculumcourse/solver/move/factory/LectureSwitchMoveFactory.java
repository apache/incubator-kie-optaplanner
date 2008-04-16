package org.drools.solver.examples.itc2007.curriculumcourse.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.examples.itc2007.curriculumcourse.solver.move.LectureSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class LectureSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) solution;
        List<Lecture> lectureList = schedule.getLectureList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Lecture> leftIt = lectureList.listIterator(); leftIt.hasNext();) {
            Lecture leftLecture = leftIt.next();
            for (ListIterator<Lecture> rightIt = lectureList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Lecture rightLecture = rightIt.next();
                if (!leftLecture.getCourse().equals(rightLecture.getCourse())) {
                    moveList.add(new LectureSwitchMove(leftLecture, rightLecture));
                }
            }
        }
        return moveList;
    }

}