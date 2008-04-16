package org.drools.solver.core.localsearch.decider.accepter;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.decider.MoveScope;

/**
 * Accepts or rejects a selected move that could be the next step.
 * Notice that it's the forager which makes the actual decision.
 * Always extend {@link AbstractAccepter} to avoid future backwards imcompatiblity issues.
 * @author Geoffrey De Smet
 */
public interface Accepter extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    /**
     * @param moveScope not null
     * @return never negative; if rejected 0.0; if accepted higher than 0.0 (ussually 1.0)
     */
    double calculateAcceptChance(MoveScope moveScope);

}
