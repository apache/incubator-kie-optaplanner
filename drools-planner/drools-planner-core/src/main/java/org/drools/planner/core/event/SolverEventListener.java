package org.drools.planner.core.event;

import java.util.EventListener;

/**
 * @author Geoffrey De Smet
 */
public interface SolverEventListener extends EventListener {

    /**
     * Called from the solver thread.
     * Should return fast, as it steals time from the Solver.
     * @param event never null
     */
    void bestSolutionChanged(BestSolutionChangedEvent event);

}
