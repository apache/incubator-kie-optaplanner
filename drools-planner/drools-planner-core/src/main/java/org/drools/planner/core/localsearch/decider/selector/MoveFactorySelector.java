package org.drools.planner.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.Decider;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.MoveFactory;

/**
 * @author Geoffrey De Smet
 */
public class MoveFactorySelector extends AbstractSelector {

    protected MoveFactory moveFactory;

    protected boolean shuffle = true;

    public void setMoveFactory(MoveFactory moveFactory) {
        this.moveFactory = moveFactory;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public void setDecider(Decider decider) {
        super.setDecider(decider);
        moveFactory.setDecider(decider);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        moveFactory.solvingStarted(localSearchSolverScope);
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        moveFactory.beforeDeciding(stepScope);
    }

    public Iterator<Move> moveIterator(StepScope stepScope) {
        return selectMoveList(stepScope).iterator();
    }

    public List<Move> selectMoveList(StepScope stepScope) {
        List<Move> moveList = moveFactory.createMoveList(stepScope.getWorkingSolution());
        if (shuffle) {
            Collections.shuffle(moveList, stepScope.getWorkingRandom());
        }
        return moveList;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        moveFactory.stepDecided(stepScope);
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        moveFactory.stepTaken(stepScope);
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        moveFactory.solvingEnded(localSearchSolverScope);
    }

}
