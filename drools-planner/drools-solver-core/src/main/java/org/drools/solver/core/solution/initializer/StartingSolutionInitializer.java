package org.drools.solver.core.solution.initializer;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;

/**
 * @author Geoffrey De Smet
 */
public interface StartingSolutionInitializer {

    boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope);

    void initializeSolution(LocalSearchSolverScope localSearchSolverScope);

}