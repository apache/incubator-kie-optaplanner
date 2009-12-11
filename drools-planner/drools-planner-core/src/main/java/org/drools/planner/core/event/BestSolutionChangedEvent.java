package org.drools.planner.core.event;

import java.util.EventObject;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.Solver;

/**
 * Delivered when the best solution changes during solving.
 * @author Geoffrey De Smet
 */
public class BestSolutionChangedEvent extends EventObject {

    private final long timeMillisSpend;
    private final Solution newBestSolution;

    /**
     * Internal API.
     * @param newBestSolution never null
     */
    public BestSolutionChangedEvent(Solver source, long timeMillisSpend, Solution newBestSolution) {
        super(source);
        this.timeMillisSpend = timeMillisSpend;
        this.newBestSolution = newBestSolution;
    }

    /**
     * @return the amount of millis spend since the solver started untill that best solution was found
     */
    public long getTimeMillisSpend() {
        return timeMillisSpend;
    }

    /**
     * @return never null
     */
    public Solution getNewBestSolution() {
        return newBestSolution;
    }

}
