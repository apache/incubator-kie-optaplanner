package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class MoveTabuAcceptor extends AbstractTabuAcceptor {

    protected boolean useUndoMoveAsTabuMove = true;

    public void setUseUndoMoveAsTabuMove(boolean useUndoMoveAsTabuMove) {
        this.useUndoMoveAsTabuMove = useUndoMoveAsTabuMove;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(MoveScope moveScope) {
        return Collections.singletonList(moveScope.getMove());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(StepScope stepScope) {
        Move tabuMove;
        if (useUndoMoveAsTabuMove) {
            tabuMove = stepScope.getUndoStep();
        } else {
            tabuMove = stepScope.getStep();
        }
        return Collections.singletonList(tabuMove);
    }

}
