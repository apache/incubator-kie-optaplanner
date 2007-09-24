package org.drools.solver.core.localsearch.finish;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;

/**
 * Superclass for CompositeFinish classes that combine multiple Finishes.
 * @author Geoffrey De Smet
 */
public abstract class AbstractCompositeFinish extends AbstractFinish implements Finish {

    protected List<Finish> finishList;

    public void setFinishList(List<Finish> finishList) {
        this.finishList = finishList;
    }

    @Override
    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        for (Finish finish : finishList) {
            finish.setLocalSearchSolver(localSearchSolver);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted() {
        for (Finish finish : finishList) {
            finish.solvingStarted();
        }
    }

    @Override
    public void beforeDeciding() {
        for (Finish finish : finishList) {
            finish.beforeDeciding();
        }
    }

    @Override
    public void stepDecided(Move step) {
        for (Finish finish : finishList) {
            finish.stepDecided(step);
        }
    }

    @Override
    public void stepTaken() {
        for (Finish finish : finishList) {
            finish.stepTaken();
        }
    }

    @Override
    public void solvingEnded() {
        for (Finish finish : finishList) {
            finish.solvingEnded();
        }
    }

}
