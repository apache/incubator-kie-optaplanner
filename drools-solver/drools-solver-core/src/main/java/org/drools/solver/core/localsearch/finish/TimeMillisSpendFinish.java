package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class TimeMillisSpendFinish extends AbstractFinish {

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

    public boolean isFinished(StepScope stepScope) {
        long timeMillisSpend = stepScope.getLocalSearchSolverScope().calculateTimeMillisSpend();
        return timeMillisSpend >= maximumTimeMillisSpend;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        long timeMillisSpend = stepScope.getLocalSearchSolverScope().calculateTimeMillisSpend();
        double timeGradient = ((double) timeMillisSpend) / ((double) maximumTimeMillisSpend);
        return Math.min(timeGradient, 1.0);
    }

}
