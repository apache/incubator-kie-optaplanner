package org.drools.solver.core.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class CachedMoveFactory extends AbstractMoveFactory {

    protected List<Move> cachedMoveList;

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        cachedMoveList = createCachedMoveList(localSearchSolverScope.getWorkingSolution());
    }

    public abstract List<Move> createCachedMoveList(Solution solution);

    @Override
    public List<Move> createMoveList(Solution solution) {
        // Shallow copy so it can be shuffled and filtered etc
        return new ArrayList<Move>(cachedMoveList);
    }

}
