package org.drools.solver.core.move.factory;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.decider.DeciderAware;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface MoveFactory extends DeciderAware, LocalSearchSolverLifecycleListener {

    List<Move> createMoveList(Solution solution);

}
