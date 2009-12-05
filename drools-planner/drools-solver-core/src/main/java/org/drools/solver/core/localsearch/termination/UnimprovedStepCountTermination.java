package org.drools.solver.core.localsearch.termination;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class UnimprovedStepCountTermination extends AbstractTermination {

    private int maximumUnimprovedStepCount = 100;

    public void setMaximumUnimprovedStepCount(int maximumUnimprovedStepCount) {
        this.maximumUnimprovedStepCount = maximumUnimprovedStepCount;
        if (maximumUnimprovedStepCount < 0) {
            throw new IllegalArgumentException("Property maximumUnimprovedStepCount (" + maximumUnimprovedStepCount
                    + ") must be greater or equal to 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************
    
    private int calculateUnimprovedStepCount(StepScope stepScope) {
        int bestStepIndex = stepScope.getLocalSearchSolverScope().getBestSolutionStepIndex();
        int stepIndex = stepScope.getStepIndex();
        return stepIndex - bestStepIndex;
    }

    public boolean isTerminated(StepScope stepScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(stepScope);
        return unimprovedStepCount >= maximumUnimprovedStepCount;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(stepScope);
        double timeGradient = ((double) unimprovedStepCount) / ((double) maximumUnimprovedStepCount);
        return Math.min(timeGradient, 1.0);
    }

}
