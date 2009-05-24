package org.drools.solver.core.localsearch.decider.accepter;

import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.DeciderAware;

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
