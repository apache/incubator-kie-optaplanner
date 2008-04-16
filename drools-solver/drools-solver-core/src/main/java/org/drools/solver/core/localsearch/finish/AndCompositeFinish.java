package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class AndCompositeFinish extends AbstractCompositeFinish {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if all the Finishes are finished.
     * @param stepScope
     */
    public boolean isFinished(StepScope stepScope) {
        for (Finish finish : finishList) {
            if (!finish.isFinished(stepScope)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the minimum timeGradient of all finishes.
     * Not supported timeGradients (-1.0) are ignored. 
     * @return the minimum timeGradient of the finishes.
     * @param stepScope
     */
    public double calculateTimeGradient(StepScope stepScope) {
        double timeGradient = 1.0;
        for (Finish finish : finishList) {
            double nextTimeGradient = finish.calculateTimeGradient(stepScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.min(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
