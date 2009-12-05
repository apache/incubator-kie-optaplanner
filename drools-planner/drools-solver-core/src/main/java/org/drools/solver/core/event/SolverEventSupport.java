package org.drools.solver.core.event;

import java.util.Iterator;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.Solver;
import org.drools.event.AbstractEventSupport;
import org.drools.event.RuleBaseEventListener;
import org.drools.event.BeforePackageAddedEvent;

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
