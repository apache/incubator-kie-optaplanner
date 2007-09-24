package org.drools.solver.core.localsearch.decider.accepter;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public interface Accepter extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    double calculateAcceptChance(Move move, double score);

}
