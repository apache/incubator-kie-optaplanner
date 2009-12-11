package org.drools.planner.core.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.solution.Solution;

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

    public List<Move> createMoveList(Solution solution) {
        // Shallow copy so it can be shuffled and filtered etc
        return new ArrayList<Move>(cachedMoveList);
    }

}
