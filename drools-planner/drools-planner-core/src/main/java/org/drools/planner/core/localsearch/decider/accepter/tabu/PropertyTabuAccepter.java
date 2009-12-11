package org.drools.planner.core.localsearch.decider.accepter.tabu;

import java.util.Collection;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;

/**
 * @author Geoffrey De Smet
 */
public class PropertyTabuAccepter extends AbstractTabuAccepter {

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
