package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class AndCompositeFinish extends AbstractCompositeFinish {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if all the Finishes are finished.
     */
    public boolean isFinished() {
        for (Finish finish : finishList) {
            if (!finish.isFinished()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the minimum timeGradient of all finishes.
     * Not supported timeGradients (-1.0) are ignored. 
     * @return the minimum timeGradient of the finishes.
     */
    public double calculateTimeGradient() {
        double timeGradient = 1.0;
        for (Finish finish : finishList) {
            double nextTimeGradient = finish.calculateTimeGradient();
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.min(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
