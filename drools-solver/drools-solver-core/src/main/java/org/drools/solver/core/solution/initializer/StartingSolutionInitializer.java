package org.drools.solver.core.solution.initializer;

import org.drools.solver.core.SolverAware;

/**
 * @author Geoffrey De Smet
 */
public interface StartingSolutionInitializer extends SolverAware {

    void intializeSolution();

}