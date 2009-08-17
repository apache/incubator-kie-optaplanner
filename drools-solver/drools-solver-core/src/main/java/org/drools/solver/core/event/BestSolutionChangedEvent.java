package org.drools.solver.core.event;

import java.util.EventObject;

import org.drools.solver.core.solution.Solution;

/**
 * Delivered when the best solution changes during solving.
 * @author Geoffrey De Smet
 */
public class BestSolutionChangedEvent extends EventObject {

    public final Solution newBestSolution;

    /**
     * Internal API.
     * @param newBestSolution never null
     */
    public BestSolutionChangedEvent(Solution newBestSolution) {
        super(newBestSolution); // TODO is this really the source of this event?
        this.newBestSolution = newBestSolution;
    }

    /**
     * @return never null
     */
    public Solution getNewBestSolution() {
        return newBestSolution;
    }

}
