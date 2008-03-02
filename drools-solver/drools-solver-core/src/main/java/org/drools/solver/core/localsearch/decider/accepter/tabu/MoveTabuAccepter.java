package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class MoveTabuAccepter extends AbstractTabuAccepter {

    protected boolean useUndoMoveAsTabuMove = true;

    public void setUseUndoMoveAsTabuMove(boolean useUndoMoveAsTabuMove) {
        this.useUndoMoveAsTabuMove = useUndoMoveAsTabuMove;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(Move move) {
        return Collections.singletonList(move);
    }

    @Override
    protected Collection<? extends Object> findNewTabu(Move step) {
        Move tabuMove;
        if (useUndoMoveAsTabuMove) {
            tabuMove = step.createUndoMove(
                    localSearchSolver.getEvaluationHandler().getStatefulSession());
        } else {
            tabuMove = step;
        }
        return Collections.singletonList(tabuMove);
    }

}
