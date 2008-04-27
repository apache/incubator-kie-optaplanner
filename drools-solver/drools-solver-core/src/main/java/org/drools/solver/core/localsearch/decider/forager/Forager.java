package org.drools.solver.core.localsearch.decider.forager;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.move.Move;

/**
 * Do not implement directly, but instead extend {@link AbstractForager}.
 * @author Geoffrey De Smet
 */
public interface Forager extends LocalSearchSolverLifecycleListener {

    void addMove(MoveScope moveScope);

    boolean isQuitEarly();

    MoveScope pickMove(StepScope stepScope);

    int getAcceptedMovesSize();

    List<Move> getTopList(int topSize);
    
}
