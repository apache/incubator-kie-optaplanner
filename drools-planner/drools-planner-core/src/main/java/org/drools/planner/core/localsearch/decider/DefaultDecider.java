package org.drools.planner.core.localsearch.decider;

import java.util.List;

import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.LocalSearchSolver;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.acceptor.Acceptor;
import org.drools.planner.core.localsearch.decider.forager.Forager;
import org.drools.planner.core.localsearch.decider.selector.Selector;
import org.drools.planner.core.localsearch.decider.deciderscorecomparator.DeciderScoreComparatorFactory;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link Decider}.
 * @author Geoffrey De Smet
 */
public class DefaultDecider implements Decider {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;

    protected DeciderScoreComparatorFactory deciderScoreComparatorFactory;
    protected Selector selector;
    protected Acceptor acceptor;
    protected Forager forager;

    protected boolean assertUndoMoveIsUncorrupted = false;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
    }

    public DeciderScoreComparatorFactory getDeciderScoreComparator() {
        return deciderScoreComparatorFactory;
    }

    public void setDeciderScoreComparator(DeciderScoreComparatorFactory deciderScoreComparator) {
        this.deciderScoreComparatorFactory = deciderScoreComparator;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
        selector.setDecider(this);
    }

    public void setAcceptor(Acceptor acceptor) {
        this.acceptor = acceptor;
    }

    public Forager getForager() {
        return forager;
    }

    public void setForager(Forager forager) {
        this.forager = forager;
    }

    public void setAssertUndoMoveIsUncorrupted(boolean assertUndoMoveIsUncorrupted) {
        this.assertUndoMoveIsUncorrupted = assertUndoMoveIsUncorrupted;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        deciderScoreComparatorFactory.solvingStarted(localSearchSolverScope);
        selector.solvingStarted(localSearchSolverScope);
        acceptor.solvingStarted(localSearchSolverScope);
        forager.solvingStarted(localSearchSolverScope);
    }

    public void beforeDeciding(StepScope stepScope) {
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        stepScope.setDeciderScoreComparator(deciderScoreComparatorFactory.createDeciderScoreComparator());
        selector.beforeDeciding(stepScope);
        acceptor.beforeDeciding(stepScope);
        forager.beforeDeciding(stepScope);
    }

    public void decideNextStep(StepScope stepScope) {
        WorkingMemory workingMemory = stepScope.getWorkingMemory();
        List<Move> moveList = selector.selectMoveList(stepScope);
        for (Move move : moveList) {
            MoveScope moveScope = new MoveScope(stepScope);
            moveScope.setMove(move);
            // Filter out not doable moves
            if (move.isMoveDoable(workingMemory)) {
                doMove(moveScope);
                if (forager.isQuitEarly()) {
                    break;
                }
            } else {
                logger.debug("    Move ({}) ignored because it is not doable.", move);
            }
        }
        MoveScope pickedMoveScope = forager.pickMove(stepScope);
        if (pickedMoveScope != null) {
            Move step = pickedMoveScope.getMove();
            stepScope.setStep(step);
            stepScope.setUndoStep(step.createUndoMove(workingMemory));
            stepScope.setScore(pickedMoveScope.getScore());
        }
    }

    private void doMove(MoveScope moveScope) {
        WorkingMemory workingMemory = moveScope.getWorkingMemory();
        Move move = moveScope.getMove();
        Move undoMove = move.createUndoMove(workingMemory);
        moveScope.setUndoMove(undoMove);
        move.doMove(workingMemory);
        processMove(moveScope);
        undoMove.doMove(workingMemory);
        if (assertUndoMoveIsUncorrupted) {
            Score undoScore = moveScope.getStepScope().getLocalSearchSolverScope().calculateScoreFromWorkingMemory();
            Score lastCompletedStepScore = moveScope.getStepScope().getLocalSearchSolverScope()
                    .getLastCompletedStepScope().getScore();
            if (!undoScore.equals(lastCompletedStepScore)) {
                throw new IllegalStateException(
                        "Corrupted undo move (" + undoMove + ") received from move (" + move + ").\n"
                        + "Unequal lastCompletedStepScore (" + lastCompletedStepScore + ") and undoScore ("
                        + undoScore + ").\n"
                        + moveScope.getStepScope().getLocalSearchSolverScope().buildConstraintOccurrenceSummary());
            }
        }
        logger.debug("    Move ({}) with score ({}) and acceptChance ({}).",
                new Object[]{moveScope.getMove(), moveScope.getScore(), moveScope.getAcceptChance()});
    }

    private void processMove(MoveScope moveScope) {
        Score score = moveScope.getStepScope().getLocalSearchSolverScope().calculateScoreFromWorkingMemory();
        moveScope.setScore(score);
        double acceptChance = acceptor.calculateAcceptChance(moveScope);
        moveScope.setAcceptChance(acceptChance);
        forager.addMove(moveScope);
    }

    public void stepDecided(StepScope stepScope) {
        deciderScoreComparatorFactory.stepDecided(stepScope);
        selector.stepDecided(stepScope);
        acceptor.stepDecided(stepScope);
        forager.stepDecided(stepScope);
    }

    public void stepTaken(StepScope stepScope) {
        deciderScoreComparatorFactory.stepTaken(stepScope);
        selector.stepTaken(stepScope);
        acceptor.stepTaken(stepScope);
        forager.stepTaken(stepScope);
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        deciderScoreComparatorFactory.solvingEnded(localSearchSolverScope);
        selector.solvingEnded(localSearchSolverScope);
        acceptor.solvingEnded(localSearchSolverScope);
        forager.solvingEnded(localSearchSolverScope);
    }

}
