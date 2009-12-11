package org.drools.planner.core.localsearch.termination;

import org.drools.planner.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class OrCompositeTermination extends AbstractCompositeTermination {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if any of the Termination is terminated.
     * @param stepScope
     */
    public boolean isTerminated(StepScope stepScope) {
        for (Termination termination : terminationList) {
            if (termination.isTerminated(stepScope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the minimum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored.
     * @return the maximum timeGradient of the Terminations.
     * @param stepScope
     */
    public double calculateTimeGradient(StepScope stepScope) {
        double timeGradient = 0.0;
        for (Termination termination : terminationList) {
            double nextTimeGradient = termination.calculateTimeGradient(stepScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
