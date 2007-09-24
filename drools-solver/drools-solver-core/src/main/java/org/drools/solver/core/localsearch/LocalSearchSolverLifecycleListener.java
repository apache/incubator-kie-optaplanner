package org.drools.solver.core.localsearch;

import org.drools.solver.core.SolverLifecycleListener;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public interface LocalSearchSolverLifecycleListener extends SolverLifecycleListener {

    void beforeDeciding();

    void stepDecided(Move step);

    void stepTaken();

}
