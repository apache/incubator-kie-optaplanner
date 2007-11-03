package org.drools.solver.core.localsearch.decider.forager;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class FirstRandomlyAcceptedForager extends AcceptionListBasedForager {

    protected Move earlyPickedMove = null;

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void beforeDeciding() {
        super.beforeDeciding();
        earlyPickedMove = null;
    }

    public void addMove(Move move, double score, double acceptChance) {
        if (acceptChance > 0.0) {
            if (localSearchSolver.getRandom().nextDouble() <= acceptChance) {
                earlyPickedMove = move;
            }
            addMoveToAcceptionList(move, score, acceptChance);
        }
    }

    public boolean isQuitEarly() {
        return earlyPickedMove != null;
    }

    public Move pickMove() {
        if (earlyPickedMove != null) {
            return earlyPickedMove;
        } else {
            return pickMaxScoreMove();
        }
    }

}