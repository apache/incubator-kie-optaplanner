package org.drools.planner.examples.itc2007.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.itc2007.examination.domain.Exam;
import org.drools.planner.examples.itc2007.examination.domain.Examination;
import org.drools.planner.examples.itc2007.examination.domain.Period;
import org.drools.planner.examples.itc2007.examination.solver.move.PeriodChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class PeriodChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Period> periodList = examination.getPeriodList();
        List<Move> moveList = new ArrayList<Move>();
        for (Exam exam : examination.getExamList()) {
            if (exam.isCoincidenceLeader()) {
                for (Period period : periodList) {
                    moveList.add(new PeriodChangeMove(exam, period));
                }
            }
        }
        return moveList;
    }

}