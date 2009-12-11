package org.drools.planner.core.localsearch.decider.forager;

import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.move.Move;

/**
 * A Forager collects the accepted moves and picks the next step from those for the Decider.
 * @see AbstractForager
 * @author Geoffrey De Smet
 */
public interface Forager extends LocalSearchSolverLifecycleListener {

    void addMove(MoveScope moveScope);

    boolean isQuitEarly();

    MoveScope pickMove(StepScope stepScope);

    int getAcceptedMovesSize();

    List<Move> getTopList(int topSize);
    
}
