package org.drools.solver.core.localsearch.termination;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class AndCompositeTermination extends AbstractCompositeTermination {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if all the Terminations are terminated.
     * @param stepScope
     */
    public boolean isTerminated(StepScope stepScope) {
        for (Termination termination : terminationList) {
            if (!termination.isTerminated(stepScope)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the minimum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored. 
     * @return the minimum timeGradient of the Terminations.
     * @param stepScope
     */
    public double calculateTimeGradient(StepScope stepScope) {
        double timeGradient = 1.0;
        for (Termination termination : terminationList) {
            double nextTimeGradient = termination.calculateTimeGradient(stepScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.min(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
