package org.drools.solver.core.localsearch.decider.accepter;

/**
 * @author Geoffrey De Smet
 */
public abstract class TimeGradientBasedAccepter extends AbstractAccepter {

    protected double timeGradient;

    // ************************************************************************
    // Worker methods
    // ************************************************************************
    
    @Override
    public void beforeDeciding() {
        timeGradient = localSearchSolver.calculateTimeGradient();
    }

}
