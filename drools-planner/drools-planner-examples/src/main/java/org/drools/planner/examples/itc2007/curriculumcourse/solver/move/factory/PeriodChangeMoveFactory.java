package org.drools.planner.examples.itc2007.curriculumcourse.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.Period;
import org.drools.planner.examples.itc2007.curriculumcourse.solver.move.PeriodChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class PeriodChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) solution;
        List<Period> periodList = schedule.getPeriodList();
        List<Move> moveList = new ArrayList<Move>();
        for (Lecture lecture : schedule.getLectureList()) {
            for (Period period : periodList) {
                moveList.add(new PeriodChangeMove(lecture, period));
            }
        }
        return moveList;
    }

}