package org.drools.solver.core.localsearch.termination;

import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;

/**
 * Abstract superclass for CompositeTermination classes that combine multiple Terminations.
 * @author Geoffrey De Smet
 */
public abstract class AbstractCompositeTermination extends AbstractTermination implements Termination {

    protected List<Termination> terminationList;

    public void setTerminationList(List<Termination> terminationList) {
        this.terminationList = terminationList;
    }

    @Override
    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        for (Termination termination : terminationList) {
            termination.setLocalSearchSolver(localSearchSolver);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        for (Termination termination : terminationList) {
            termination.solvingStarted(localSearchSolverScope);
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        for (Termination termination : terminationList) {
            termination.beforeDeciding(stepScope);
        }
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        for (Termination termination : terminationList) {
            termination.stepDecided(stepScope);
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        for (Termination termination : terminationList) {
            termination.stepTaken(stepScope);
        }
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        for (Termination termination : terminationList) {
            termination.solvingEnded(localSearchSolverScope);
        }
    }

}
