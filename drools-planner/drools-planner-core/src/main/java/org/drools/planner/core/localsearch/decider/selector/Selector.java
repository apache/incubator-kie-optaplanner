package org.drools.planner.core.localsearch.decider.selector;

import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.DeciderAware;
import org.drools.planner.core.move.Move;

/**
 * A Selector selects or generates moves for the Decider.
 * @see AbstractSelector
 * @author Geoffrey De Smet
 */
public interface Selector extends DeciderAware, LocalSearchSolverLifecycleListener {

    List<Move> selectMoveList(StepScope stepScope);

}