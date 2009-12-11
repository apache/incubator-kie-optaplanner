package org.drools.planner.core.solution.initializer;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;

/**
 * @author Geoffrey De Smet
 */
public interface StartingSolutionInitializer {

    boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope);

    void initializeSolution(LocalSearchSolverScope localSearchSolverScope);

}