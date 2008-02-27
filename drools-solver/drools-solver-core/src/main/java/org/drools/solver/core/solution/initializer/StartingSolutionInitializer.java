package org.drools.solver.core.solution.initializer;

import org.drools.solver.core.SolverAware;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface StartingSolutionInitializer extends SolverAware {

    boolean isSolutionInitialized(Solution solution);

    void initializeSolution(Solution solution);

}