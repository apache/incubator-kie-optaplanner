package org.drools.planner.core.localsearch.termination;

import org.drools.planner.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class TimeMillisSpendTermination extends AbstractTermination {

    private long maximumTimeMillisSpend;

    public void setMaximumTimeMillisSpend(long maximumTimeMillisSpend) {
        this.maximumTimeMillisSpend = maximumTimeMillisSpend;
        if (maximumTimeMillisSpend <= 0L) {
            throw new IllegalArgumentException("Property maximumTimeMillisSpend (" + maximumTimeMillisSpend
                    + ") must be greater than 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isTerminated(StepScope stepScope) {
        long timeMillisSpend = stepScope.getLocalSearchSolverScope().calculateTimeMillisSpend();
        return timeMillisSpend >= maximumTimeMillisSpend;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        long timeMillisSpend = stepScope.getLocalSearchSolverScope().calculateTimeMillisSpend();
        double timeGradient = ((double) timeMillisSpend) / ((double) maximumTimeMillisSpend);
        return Math.min(timeGradient, 1.0);
    }

}
