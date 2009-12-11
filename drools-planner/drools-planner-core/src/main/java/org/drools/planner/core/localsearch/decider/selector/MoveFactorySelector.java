package org.drools.planner.core.localsearch.decider.selector;

import java.util.Collections;
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
    protected Double relativeSelection = null;

    public void setMoveFactory(MoveFactory moveFactory) {
        this.moveFactory = moveFactory;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void setRelativeSelection(Double relativeSelection) {
        this.relativeSelection = relativeSelection;
        if (relativeSelection < 0.0 || relativeSelection > 1.0) {
            throw new IllegalArgumentException( "The selector's relativeSelection (" + relativeSelection
                    + ") is not in the range [0.0,1.0].");
        }
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

    public final List<Move> selectMoveList(StepScope stepScope) {
        List<Move> moveList = moveFactory.createMoveList(stepScope.getWorkingSolution());
        if (shuffle) {
            Collections.shuffle(moveList, stepScope.getWorkingRandom());
        }
        if (relativeSelection != null) {
            int selectionSize = (int) Math.ceil(relativeSelection * moveList.size());
            if (selectionSize == 0 && !moveList.isEmpty()) {
                selectionSize = 1;
            }
            moveList = moveList.subList(0, selectionSize);
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