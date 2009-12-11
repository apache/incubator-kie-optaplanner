package org.drools.planner.core.move.factory;

import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.decider.DeciderAware;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface MoveFactory extends DeciderAware, LocalSearchSolverLifecycleListener {

    List<Move> createMoveList(Solution solution);

}
