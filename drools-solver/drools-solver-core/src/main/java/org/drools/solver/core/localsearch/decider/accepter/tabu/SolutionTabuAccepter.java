package org.drools.solver.core.localsearch.decider.accepter.tabu;

import java.util.Collections;
import java.util.List;

import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class SolutionTabuAccepter extends AbstractTabuAccepter {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected List<? extends Object> findTabu(Move move) {
        return Collections.singletonList(localSearchSolver.getCurrentSolution());
    }

    @Override
    protected List<? extends Object> findNewTabu(Move step) {
        return Collections.singletonList(localSearchSolver.getCurrentSolution().cloneSolution());
    }
    
}
