package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.List;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class PropertyTabuAccepter extends AbstractTabuAccepter {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected List<? extends Object> findTabu(Move move) {
        TabuPropertyEnabled tabuPropertyEnabled = (TabuPropertyEnabled) move;
        return tabuPropertyEnabled.getTabuPropertyList();
    }

}
