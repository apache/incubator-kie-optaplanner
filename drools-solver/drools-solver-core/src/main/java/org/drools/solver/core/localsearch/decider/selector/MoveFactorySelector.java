package org.drools.solver.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.MoveFactory;

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
    }

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        moveFactory.setLocalSearchSolver(localSearchSolver);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted() {
        moveFactory.solvingStarted();
    }

    public void beforeDeciding() {
        moveFactory.beforeDeciding();
    }

    public final List<Move> selectMoveList() {
        List<Move> moveList = moveFactory.createMoveList(localSearchSolver.getCurrentSolution());
        if (shuffle) {
            Collections.shuffle(moveList, localSearchSolver.getRandom());
        }
        if (relativeSelection != null) {
            int selectionSize = (int) Math.ceil(relativeSelection * moveList.size());
            if (selectionSize == 0) {
                selectionSize = 1;
            }
            moveList = moveList.subList(0, selectionSize);
        }
        return moveList;
    }

    public void stepDecided(Move step) {
        moveFactory.stepDecided(step);
    }

    public void stepTaken() {
        moveFactory.stepTaken();
    }

    public void solvingEnded() {
        moveFactory.solvingEnded();
    }

}