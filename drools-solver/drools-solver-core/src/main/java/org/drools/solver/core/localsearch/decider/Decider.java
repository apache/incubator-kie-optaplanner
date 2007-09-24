package org.drools.solver.core.localsearch.decider;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public interface Decider extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    Move decideNextStep();

}
