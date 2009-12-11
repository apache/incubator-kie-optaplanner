package org.drools.planner.core.localsearch.decider.accepter;

import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.decider.MoveScope;

/**
 * An Accepter accepts or rejects a selected move for the Decider.
 * Notice that the Forager can still ignore the advice of the Accepter.
 * @see AbstractAccepter
 * @author Geoffrey De Smet
 */
public interface Accepter extends LocalSearchSolverLifecycleListener {

    /**
     * @param moveScope not null
     * @return never negative; if rejected 0.0; if accepted higher than 0.0 (ussually 1.0)
     */
    double calculateAcceptChance(MoveScope moveScope);

}
