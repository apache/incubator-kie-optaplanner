package org.drools.planner.examples.itc2007.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.itc2007.examination.domain.Exam;
import org.drools.planner.examples.itc2007.examination.domain.Examination;
import org.drools.planner.examples.itc2007.examination.solver.move.ExamSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class ExamSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Exam> examList = examination.getExamList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Exam> leftIt = examList.listIterator(); leftIt.hasNext();) {
            Exam leftExam = leftIt.next();
            for (ListIterator<Exam> rightIt = examList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Exam rightExam = rightIt.next();
                moveList.add(new ExamSwitchMove(leftExam, rightExam));
            }
        }
        return moveList;
    }

}