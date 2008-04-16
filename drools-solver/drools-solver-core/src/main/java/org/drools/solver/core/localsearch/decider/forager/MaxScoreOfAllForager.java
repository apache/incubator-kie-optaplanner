package org.drools.solver.core.localsearch.decider.forager;

import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class MaxScoreOfAllForager extends AcceptedListBasedForager {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void addMove(MoveScope moveScope) {
        if (moveScope.getAcceptChance() > 0.0) {
            addMoveScopeToAcceptedList(moveScope);
        }
    }

    @Override
    public MoveScope pickMove(StepScope stepScope) {
        return pickMaxScoreMoveScopeFromAcceptedList(stepScope);
    }

}
