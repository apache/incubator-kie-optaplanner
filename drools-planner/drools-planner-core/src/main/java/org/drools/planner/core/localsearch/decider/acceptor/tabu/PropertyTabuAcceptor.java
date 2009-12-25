package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class PropertyTabuAcceptor extends AbstractTabuAcceptor {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(MoveScope moveScope) {
        TabuPropertyEnabled tabuPropertyEnabled = (TabuPropertyEnabled) moveScope.getMove();
        return tabuPropertyEnabled.getTabuProperties();
    }

    @Override
    protected Collection<? extends Object> findNewTabu(StepScope stepScope) {
        TabuPropertyEnabled tabuPropertyEnabled = (TabuPropertyEnabled) stepScope.getStep();
        return tabuPropertyEnabled.getTabuProperties();
    }

}
