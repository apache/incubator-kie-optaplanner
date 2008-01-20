package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class SolutionTabuAccepter extends AbstractTabuAccepter {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(Move move) {
        return Collections.singletonList(localSearchSolver.getCurrentSolution());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(Move step) {
        return Collections.singletonList(localSearchSolver.getCurrentSolution().cloneSolution());
    }
    
}
