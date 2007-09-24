package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class OrCompositeFinish extends AbstractCompositeFinish {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @return true if any of the Finishes is finished.
     */
    public boolean isFinished() {
        for (Finish finish : finishList) {
            if (finish.isFinished()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the minimum timeGradient of all finishes.
     * Not supported timeGradients (-1.0) are ignored.
     * @return the maximum timeGradient of the finishes.
     */
    public double calculateTimeGradient() {
        double timeGradient = 0.0;
        for (Finish finish : finishList) {
            double nextTimeGradient = finish.calculateTimeGradient();
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
