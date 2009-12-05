package org.drools.solver.core.localsearch.decider.selector;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.DeciderAware;
import org.drools.solver.core.move.Move;

/**
 * A Selector selects or generates moves for the Decider.
 * @see AbstractSelector
 * @author Geoffrey De Smet
 */
public interface Selector extends DeciderAware, LocalSearchSolverLifecycleListener {

    List<Move> selectMoveList(StepScope stepScope);

}