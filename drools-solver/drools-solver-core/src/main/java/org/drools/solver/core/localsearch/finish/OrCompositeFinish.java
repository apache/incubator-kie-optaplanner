package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class OrCompositeFinish extends AbstractCompositeFinish {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if any of the Finishes is finished.
     * @param stepScope
     */
    public boolean isFinished(StepScope stepScope) {
        for (Finish finish : finishList) {
            if (finish.isFinished(stepScope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the minimum timeGradient of all finishes.
     * Not supported timeGradients (-1.0) are ignored.
     * @return the maximum timeGradient of the finishes.
     * @param stepScope
     */
    public double calculateTimeGradient(StepScope stepScope) {
        double timeGradient = 0.0;
        for (Finish finish : finishList) {
            double nextTimeGradient = finish.calculateTimeGradient(stepScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
