package org.drools.solver.core.localsearch.decider;

import java.util.List;

import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.accepter.Accepter;
import org.drools.solver.core.localsearch.decider.forager.Forager;
import org.drools.solver.core.localsearch.decider.selector.Selector;
import org.drools.solver.core.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class DefaultDecider implements Decider {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;

    protected Selector selector;
    protected Accepter accepter;
    protected Forager forager;

    protected boolean verifyUndoMoveIsUncorrupted = false;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
        selector.setLocalSearchSolver(localSearchSolver);
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setAccepter(Accepter accepter) {
        this.accepter = accepter;
    }

    public Forager getForager() {
        return forager;
    }

    public void setForager(Forager forager) {
        this.forager = forager;
    }

    public void setVerifyUndoMoveIsUncorrupted(boolean verifyUndoMoveIsUncorrupted) {
        this.verifyUndoMoveIsUncorrupted = verifyUndoMoveIsUncorrupted;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        selector.solvingStarted(localSearchSolverScope);
        accepter.solvingStarted(localSearchSolverScope);
        forager.solvingStarted(localSearchSolverScope);
    }

    public void beforeDeciding(StepScope stepScope) {
        selector.beforeDeciding(stepScope);
        accepter.beforeDeciding(stepScope);
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
        if (verifyUndoMoveIsUncorrupted) {
            double undoScore = moveScope.getStepScope().getLocalSearchSolverScope().calculateScoreFromWorkingMemory();
            if (undoScore != moveScope.getStepScope().getLocalSearchSolverScope()
                    .getLastCompletedStepScope().getScore()) {
                throw new IllegalStateException(
                        "Corrupted undo move (" + undoMove + ") received from move (" + move + ").");
            }
        }
        logger.debug("    Move ({}) with score ({}) and acceptChance ({}).",
                new Object[]{moveScope.getMove(), moveScope.getScore(), moveScope.getAcceptChance()});
    }

    private void processMove(MoveScope moveScope) {
        double score = moveScope.getStepScope().getLocalSearchSolverScope().calculateScoreFromWorkingMemory();
        moveScope.setScore(score);
        double acceptChance = accepter.calculateAcceptChance(moveScope);
        moveScope.setAcceptChance(acceptChance);
        forager.addMove(moveScope);
    }

    public void stepDecided(StepScope stepScope) {
        selector.stepDecided(stepScope);
        accepter.stepDecided(stepScope);
        forager.stepDecided(stepScope);
    }

    public void stepTaken(StepScope stepScope) {
        selector.stepTaken(stepScope);
        accepter.stepTaken(stepScope);
        forager.stepTaken(stepScope);
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        selector.solvingEnded(localSearchSolverScope);
        accepter.solvingEnded(localSearchSolverScope);
        forager.solvingEnded(localSearchSolverScope);
    }

}
