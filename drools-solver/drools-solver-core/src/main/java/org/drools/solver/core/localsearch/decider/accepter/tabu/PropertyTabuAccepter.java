package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class PropertyTabuAccepter extends AbstractTabuAccepter {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(Move move) {
        TabuPropertyEnabled tabuPropertyEnabled = (TabuPropertyEnabled) move;
        return tabuPropertyEnabled.getTabuProperties();
    }

}
