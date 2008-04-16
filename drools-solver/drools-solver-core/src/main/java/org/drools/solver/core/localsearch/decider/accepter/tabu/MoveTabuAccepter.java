package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
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
    protected Collection<? extends Object> findTabu(MoveScope moveScope) {
        return Collections.singletonList(moveScope.getMove());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(StepScope stepScope) {
        Move tabuMove;
        Move step = stepScope.getStep();
        if (useUndoMoveAsTabuMove) {
            // In stepTaken this the undoMove would be corrupted
            tabuMove = step.createUndoMove(stepScope.getWorkingMemory());
        } else {
            tabuMove = step;
        }
        return Collections.singletonList(tabuMove);
    }

}
