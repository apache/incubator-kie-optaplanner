package org.drools.solver.core.localsearch.decider.selector;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public interface Selector extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    List<Move> selectMoveList();

}