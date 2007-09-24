package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class StepCountFinish extends AbstractFinish {

    private int maximumStepCount = 100;

    public void setMaximumStepCount(int maximumStepCount) {
        this.maximumStepCount = maximumStepCount;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isFinished() {
        int stepIndex = localSearchSolver.getStepIndex();
        return stepIndex >= maximumStepCount;
    }

    public double calculateTimeGradient() {
        int stepIndex = localSearchSolver.getStepIndex();
        double timeGradient = ((double) stepIndex) / ((double) maximumStepCount);
        return Math.min(timeGradient, 1.0);
    }

}
