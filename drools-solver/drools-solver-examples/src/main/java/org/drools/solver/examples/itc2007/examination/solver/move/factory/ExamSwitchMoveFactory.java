package org.drools.solver.examples.itc2007.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.solver.move.ExamSwitchMove;

/**
 * @author Geoffrey De Smet
 */
public class ExamSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Examination examination = (Examination) localSearchSolver.getCurrentSolution();
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