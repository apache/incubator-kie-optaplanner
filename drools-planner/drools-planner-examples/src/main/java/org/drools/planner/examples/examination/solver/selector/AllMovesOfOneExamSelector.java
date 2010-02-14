package org.drools.planner.examples.examination.solver.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.Decider;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.localsearch.decider.selector.AbstractSelector;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.MoveFactory;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Examination;
import org.drools.planner.examples.examination.solver.move.factory.ExamSwitchMoveFactory;
import org.drools.planner.examples.examination.solver.move.factory.PeriodChangeMoveFactory;
import org.drools.planner.examples.examination.solver.move.factory.RoomChangeMoveFactory;

/**
 * A custom selector implementation for the Examination example.
 * @author Geoffrey De Smet
 */
public class AllMovesOfOneExamSelector extends AbstractSelector {

    protected PeriodChangeMoveFactory periodChangeMoveFactory = new PeriodChangeMoveFactory();
    protected RoomChangeMoveFactory roomChangeMoveFactory = new RoomChangeMoveFactory();
    protected ExamSwitchMoveFactory examSwitchMoveFactory = new ExamSwitchMoveFactory();

    protected Map<Exam, List<Move>> cachedExamToMoveMap;
    protected List<Exam> shuffledExamList;
    protected int nextShuffledExamListIndex;

    @Override
    public void setDecider(Decider decider) {
        super.setDecider(decider);
        periodChangeMoveFactory.setDecider(decider);
        roomChangeMoveFactory.setDecider(decider);
        examSwitchMoveFactory.setDecider(decider);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        periodChangeMoveFactory.solvingStarted(localSearchSolverScope);
        roomChangeMoveFactory.solvingStarted(localSearchSolverScope);
        examSwitchMoveFactory.solvingStarted(localSearchSolverScope);
        createCachedExamToMoveMap(localSearchSolverScope);
    }

    private void createCachedExamToMoveMap(LocalSearchSolverScope localSearchSolverScope) {
        Examination examination = (Examination) localSearchSolverScope.getWorkingSolution();
        int examListSize = examination.getExamList().size();
        List<Move> cachedPeriodChangeMoveList = periodChangeMoveFactory.getCachedMoveList();
        List<Move> cachedRoomChangeMoveList = roomChangeMoveFactory.getCachedMoveList();
        List<Move> cachedExamSwitchMoveList = examSwitchMoveFactory.getCachedMoveList();
        cachedExamToMoveMap = new HashMap<Exam, List<Move>>(cachedPeriodChangeMoveList.size()
                + cachedRoomChangeMoveList.size() + cachedExamSwitchMoveList.size());
        addToCachedExamToMoveMap(examListSize, cachedPeriodChangeMoveList);
        addToCachedExamToMoveMap(examListSize, cachedRoomChangeMoveList);
        addToCachedExamToMoveMap(examListSize, cachedExamSwitchMoveList);
        shuffledExamList = new ArrayList<Exam>(cachedExamToMoveMap.keySet());
        // shuffling is lazy (just in time in the selectMoveList method)
        nextShuffledExamListIndex = Integer.MAX_VALUE;
    }

    private void addToCachedExamToMoveMap(int examListSize, List<Move> cachedMoveList) {
        for (Move cachedMove : cachedMoveList) {
            TabuPropertyEnabled tabuPropertyEnabledMove = (TabuPropertyEnabled) cachedMove;
            for (Object o : tabuPropertyEnabledMove.getTabuProperties()) {
                Exam exam = (Exam) o;
                List<Move> moveList = cachedExamToMoveMap.get(exam);
                if (moveList == null) {
                    moveList = new ArrayList<Move>(examListSize);
                    cachedExamToMoveMap.put(exam, moveList);
                }
                moveList.add(cachedMove);
            }
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        periodChangeMoveFactory.beforeDeciding(stepScope);
        roomChangeMoveFactory.beforeDeciding(stepScope);
        examSwitchMoveFactory.beforeDeciding(stepScope);
    }

    public List<Move> selectMoveList(StepScope stepScope) {
        if (nextShuffledExamListIndex >= shuffledExamList.size()) {
            // Just in time shuffling
            Collections.shuffle(shuffledExamList, stepScope.getWorkingRandom());
            nextShuffledExamListIndex = 0;
        }
        Exam exam = shuffledExamList.get(nextShuffledExamListIndex);
        List<Move> moveList = cachedExamToMoveMap.get(exam);
        nextShuffledExamListIndex++;
        return moveList;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        periodChangeMoveFactory.stepDecided(stepScope);
        roomChangeMoveFactory.stepDecided(stepScope);
        examSwitchMoveFactory.stepDecided(stepScope);
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        periodChangeMoveFactory.stepTaken(stepScope);
        roomChangeMoveFactory.stepTaken(stepScope);
        examSwitchMoveFactory.stepTaken(stepScope);
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        periodChangeMoveFactory.solvingEnded(localSearchSolverScope);
        roomChangeMoveFactory.solvingEnded(localSearchSolverScope);
        examSwitchMoveFactory.solvingEnded(localSearchSolverScope);
    }

}
