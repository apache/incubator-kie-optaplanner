package org.drools.solver.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class CachedMoveListMoveFactory extends AbstractMoveFactory {

    protected List<Move> cachedMoveList;
    protected boolean shuffleListEveryStep = false;

    public List<Move> getCachedMoveList() {
        return cachedMoveList;
    }

    public boolean isShuffleListEveryStep() {
        return shuffleListEveryStep;
    }

    public void setShuffleListEveryStep(boolean shuffleListEveryStep) {
        this.shuffleListEveryStep = shuffleListEveryStep;
    }

    @Override
    public void solvingStarted() {
        cachedMoveList = createMoveList(localSearchSolver.getCurrentSolution());
    }

    public abstract List<Move> createMoveList(Solution startingSolution);

    public Iterator<Move> iterator() {
        if (shuffleListEveryStep) {
            Collections.shuffle(cachedMoveList, localSearchSolver.getRandom()); // TODO is this needed?
        }
        return cachedMoveList.iterator();
    }

}
