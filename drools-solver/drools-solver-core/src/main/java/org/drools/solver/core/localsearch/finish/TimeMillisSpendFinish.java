package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class TimeMillisSpendFinish extends AbstractFinish {

    private long maximumTimeMillisSpend;

    public void setMaximumTimeMillisSpend(long maximumTimeMillisSpend) {
        this.maximumTimeMillisSpend = maximumTimeMillisSpend;
        if (maximumTimeMillisSpend <= 0L) {
            throw new IllegalArgumentException("Property maximumTimeMillisSpend (" + maximumTimeMillisSpend
                    + ") must be greater then 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isFinished() {
        long timeMillisSpend = localSearchSolver.getTimeMillisSpend();
        return timeMillisSpend >= maximumTimeMillisSpend;
    }

    public double calculateTimeGradient() {
        long timeMillisSpend = localSearchSolver.getTimeMillisSpend();
        double timeGradient = ((double) timeMillisSpend) / ((double) maximumTimeMillisSpend);
        return Math.min(timeGradient, 1.0);
    }

}
