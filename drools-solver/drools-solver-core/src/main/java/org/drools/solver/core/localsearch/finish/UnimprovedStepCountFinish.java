package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class UnimprovedStepCountFinish extends AbstractFinish {

    private int maximumUnimprovedStepCount = 100;

    public void setMaximumUnimprovedStepCount(int maximumUnimprovedStepCount) {
        this.maximumUnimprovedStepCount = maximumUnimprovedStepCount;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************
    
    private int getUnimprovedStepCount() {
        int improvedStepIndex = localSearchSolver.getBestSolutionStepIndex();
        int stepIndex = localSearchSolver.getStepIndex();
        return stepIndex - improvedStepIndex;
    }

    public boolean isFinished() {
        int unimprovedStepCount = getUnimprovedStepCount();
        return unimprovedStepCount >= maximumUnimprovedStepCount;
    }

    public double calculateTimeGradient() {
        int unimprovedStepCount = getUnimprovedStepCount();
        double timeGradient = ((double) unimprovedStepCount) / ((double) maximumUnimprovedStepCount);
        return Math.min(timeGradient, 1.0);
    }

}
