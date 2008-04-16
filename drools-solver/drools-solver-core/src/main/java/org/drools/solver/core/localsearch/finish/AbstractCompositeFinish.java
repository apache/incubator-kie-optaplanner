package org.drools.solver.core.localsearch.finish;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;

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
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        for (Finish finish : finishList) {
            finish.solvingStarted(localSearchSolverScope);
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        for (Finish finish : finishList) {
            finish.beforeDeciding(stepScope);
        }
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        for (Finish finish : finishList) {
            finish.stepDecided(stepScope);
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        for (Finish finish : finishList) {
            finish.stepTaken(stepScope);
        }
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        for (Finish finish : finishList) {
            finish.solvingEnded(localSearchSolverScope);
        }
    }

}
