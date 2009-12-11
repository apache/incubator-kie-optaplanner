package org.drools.planner.core.event;

import java.util.Iterator;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.Solver;
import org.drools.event.AbstractEventSupport;

/**
 * Internal API.
 * @author Geoffrey De Smet
 */
public class SolverEventSupport extends AbstractEventSupport<SolverEventListener> {

    private Solver solver;

    public SolverEventSupport(Solver solver) {
        this.solver = solver;
    }

    public void fireBestSolutionChanged(Solution newBestSolution) {
        final Iterator<SolverEventListener> iter = getEventListenersIterator();
        if (iter.hasNext()) {
            final BestSolutionChangedEvent event = new BestSolutionChangedEvent(solver,
                    solver.getTimeMillisSpend(), newBestSolution);
            do {
                iter.next().bestSolutionChanged(event);
            } while (iter.hasNext());
        }
    }

}
