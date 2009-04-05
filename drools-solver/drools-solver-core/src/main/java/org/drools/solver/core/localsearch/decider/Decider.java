package org.drools.solver.core.localsearch.decider;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.forager.Forager;

/**
 * A decider decides the next step.
 * @see DefaultDecider
 * @author Geoffrey De Smet
 */
public interface Decider extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    void decideNextStep(StepScope stepScope);

    Forager getForager();

}
