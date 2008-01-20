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
        if (useUndoMoveAsTabuMove) {
            return Collections.singletonList(step.createUndoMove(
                    localSearchSolver.getEvaluationHandler().getStatefulSession()));
        } else {
            return Collections.singletonList(step);
        }
    }

}
