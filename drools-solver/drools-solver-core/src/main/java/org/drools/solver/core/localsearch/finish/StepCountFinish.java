package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class StepCountFinish extends AbstractFinish {

    private int maximumStepCount = 100;

    public void setMaximumStepCount(int maximumStepCount) {
        this.maximumStepCount = maximumStepCount;
        if (maximumStepCount < 0) {
            throw new IllegalArgumentException("Property maximumStepCount (" + maximumStepCount
                    + ") must be greater or equal to 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isFinished(StepScope stepScope) {
        int stepIndex = stepScope.getStepIndex();
        return stepIndex >= maximumStepCount;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        int stepIndex = stepScope.getStepIndex();
        double timeGradient = ((double) stepIndex) / ((double) maximumStepCount);
        return Math.min(timeGradient, 1.0);
    }

}
