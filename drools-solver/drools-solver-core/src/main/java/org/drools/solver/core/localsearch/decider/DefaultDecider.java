package org.drools.solver.core.localsearch.decider;

import org.drools.WorkingMemory;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.decider.accepter.Accepter;
import org.drools.solver.core.localsearch.decider.forager.Forager;
import org.drools.solver.core.localsearch.decider.selector.MoveFactory;
import org.drools.solver.core.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class DefaultDecider implements Decider {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;

    protected MoveFactory moveFactory;
    protected Accepter accepter;
    protected Forager forager;

    protected boolean verifyUndoMoveIsUncorrupted = false;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
        moveFactory.setLocalSearchSolver(localSearchSolver);
        accepter.setLocalSearchSolver(localSearchSolver);
        forager.setLocalSearchSolver(localSearchSolver);
    }

    public void setMoveFactory(MoveFactory moveFactory) {
        this.moveFactory = moveFactory;
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
        moveFactory.solvingStarted();
        accepter.solvingStarted();
        forager.solvingStarted();
    }

    public void beforeDeciding() {
        moveFactory.beforeDeciding();
        accepter.beforeDeciding();
        forager.beforeDeciding();
    }

    public Move decideNextStep() {
        WorkingMemory workingMemory = localSearchSolver.getEvaluationHandler().getStatefulSession();
        for (Move move : moveFactory) {
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

//    /**
//     * @param move
//     * @param score
//     * @return true if the move is accepted
//     */
//    private boolean decideOnMove(Move move, double score) { // TODO FIXME
//        // Filter out not fit enough moves
//        if (!(deciderMode.isTakeFittestOnly() && (nextMove != null) && (nextScore > score))) {
//            double acceptChance = accepter.calculateAcceptChance(move, score);
//            // Filter out not acceptable moves
//            if (acceptChance > 0.0) {
//                double random = getRandom().nextDouble();
//                // Filter out not accepted moves
//                if (random < acceptChance) {
//                    if (deciderMode.isTakeFittestOnly() && (nextMove != null)
//                            && (score > nextScore)) {
//                        acceptedCount = 0.0;
//                    }
//                    acceptedCount++;
//                    if (random < (acceptChance / acceptedCount)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

//    private void processMove(Move move, double score) {
//        if (decideOnMove(move, score)) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("    Move (" + move + ") with score (" +  score + ") accepted. "
//                        + "Updating next move and score.");
//            }
//            nextMove = move;
//            nextScore = score;
//            if (deciderMode.isTakeFirstFound()) {
//                // Only break early if any will do or if it improves (because only improving will do)
//                if (deciderMode.isTakeFittestOnly()) {
//                    quitEarly = true;
//                } else if (score > localSearchSolver.getStepScore()) {
//                    quitEarly = true;
//                }
//            }
//        } else {
//            if (logger.isDebugEnabled()) {
//                logger.debug("    Move (" + move + ") with score (" +  score + ") is not accepted.");
//            }
//        }
//    }

//    /**
//     * @param move
//     * @param score
//     * @return true if the move is accepted
//     */
//    protected boolean decideOnMove(Move move, double score) {
//        // Filter out not fit enough moves
//        if (!(deciderMode.isTakeFittestOnly() && (nextMove != null) && (nextScore > score))) {
//            double acceptChance = accepter.calculateAcceptChance(move, score);
//            // Filter out not acceptable moves
//            if (acceptChance > 0.0) {
//                double random = getRandom().nextDouble();
//                // Filter out not accepted moves
//                if (random < acceptChance) {
//                    if (deciderMode.isTakeFittestOnly() && (nextMove != null)
//                            && (score > nextScore)) {
//                        acceptedCount = 0.0;
//                    }
//                    acceptedCount++;
//                    if (random < (acceptChance / acceptedCount)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    public void stepDecided(Move step) {
        moveFactory.stepDecided(step);
        accepter.stepDecided(step);
        forager.stepDecided(step);
    }

    public void stepTaken() {
        moveFactory.stepTaken();
        accepter.stepTaken();
        forager.stepTaken();
    }

    public void solvingEnded() {
        moveFactory.solvingEnded();
        accepter.solvingEnded();
        forager.solvingEnded();
    }

}
