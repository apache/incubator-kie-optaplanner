package org.drools.solver.core.localsearch.decider;

import org.drools.solver.core.localsearch.LocalSearchSolver;

/**
 * @author Geoffrey De Smet
 */
public interface DeciderAware {

    void setDecider(Decider decider);

}