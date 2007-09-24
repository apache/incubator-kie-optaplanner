package org.drools.solver.core.localsearch.decider.forager;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public interface Forager extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    void addMove(Move move, double score, double acceptChance);

    boolean isQuitEarly();

    Move pickMove();

    List<Move> getTopList(int topSize);
    
}
