package org.drools.solver.core.localsearch.decider;

import org.drools.WorkingMemory;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.localsearch.LocalSearchSolver;
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
        accepter.setLocalSearchSolver(localSearchSolver);
        forager.setLocalSearchSolver(localSearchSolver);
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setAccepter(Accepter accepter) {
        this.accepter = accepter;
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

    public void solvingStarted() {
        selector.solvingStarted();
        accepter.solvingStarted();
        forager.solvingStarted();
    }

    public void beforeDeciding() {
        selector.beforeDeciding();
        accepter.beforeDeciding();
        forager.beforeDeciding();
    }

    public Move decideNextStep() {
        WorkingMemory workingMemory = localSearchSolver.getEvaluationHandler().getStatefulSession();
        for (Move move : selector.selectMoveList()) {
            // Filter out not doable moves
            if (move.isMoveDoable(workingMemory)) {
                doMove(move);
            } else {
                logger.debug("    Move ({}) ignored because not doable.", move);
            }
            if (forager.isQuitEarly()) {
                break;
            }
        }
        return forager.pickMove();
    }

    private void doMove(Move move) {
        WorkingMemory workingMemory = localSearchSolver.getEvaluationHandler().getStatefulSession();
        Move undoMove = move.createUndoMove(workingMemory);
        move.doMove(workingMemory);
        processMove(move);
        undoMove.doMove(workingMemory);
        if (verifyUndoMoveIsUncorrupted) {
            double undoScore = localSearchSolver.getEvaluationHandler().fireAllRulesAndCalculateDecisionScore();
            if (undoScore != localSearchSolver.getStepScore()) {
                throw new IllegalStateException(
                        "Corrupted undo move (" + undoMove + ") received from move (" + move + ").");
            }
        }
    }

    private void processMove(Move move) {
        EvaluationHandler evaluationHandler = localSearchSolver.getEvaluationHandler();
        double score = evaluationHandler.fireAllRulesAndCalculateDecisionScore();
        double acceptChance = accepter.calculateAcceptChance(move, score);
        // TODO the move's toString() is ussually wrong because doMove has already been called
        logger.debug("    Move ({}) with score ({}) and acceptChance ({}).", new Object[] {move, score, acceptChance});
        forager.addMove(move, score, acceptChance);
    }

    public void stepDecided(Move step) {
        selector.stepDecided(step);
        accepter.stepDecided(step);
        forager.stepDecided(step);
    }

    public void stepTaken() {
        selector.stepTaken();
        accepter.stepTaken();
        forager.stepTaken();
    }

    public void solvingEnded() {
        selector.solvingEnded();
        accepter.solvingEnded();
        forager.solvingEnded();
    }

}
