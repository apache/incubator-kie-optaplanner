package org.drools.solver.core.localsearch.decider.forager;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class FirstRandomlyAcceptedForager extends AcceptedListBasedForager {

    protected MoveScope earlyPickedMoveScope = null;

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void beforeDeciding(StepScope stepScope) {
        super.beforeDeciding(stepScope);
        earlyPickedMoveScope = null;
    }

    @Override
    public void addMove(MoveScope moveScope) {
        if (moveScope.getAcceptChance() > 0.0) {
            double randomChance = moveScope.getWorkingRandom().nextDouble();
            if (randomChance <= moveScope.getAcceptChance()) {
                earlyPickedMoveScope = moveScope;
            }
            addMoveScopeToAcceptedList(moveScope);
        }
    }

    @Override
    public boolean isQuitEarly() {
        return earlyPickedMoveScope != null;
    }

    @Override
    public MoveScope pickMove(StepScope stepScope) {
        if (earlyPickedMoveScope != null) {
            return earlyPickedMoveScope;
        } else {
            return pickMaxScoreMoveScopeFromAcceptedList(stepScope);
        }
    }

}