package org.drools.solver.core.localsearch;

/**
 * @author Geoffrey De Smet
 */
public interface LocalSearchSolverAware {

    /**
     * Called during configuration to set
     */
    void setLocalSearchSolver(LocalSearchSolver localSearchSolver);

}
