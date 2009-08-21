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

    public void fireBestSolutionChanged(Solver source, long timeMillisSpend, Solution newBestSolution) {
        final Iterator<SolverEventListener> iter = getEventListenersIterator();
        if (iter.hasNext()) {
            final BestSolutionChangedEvent event = new BestSolutionChangedEvent(
                    source, timeMillisSpend, newBestSolution);
            do {
                iter.next().bestSolutionChanged(event);
            } while (iter.hasNext());
        }
    }

}
