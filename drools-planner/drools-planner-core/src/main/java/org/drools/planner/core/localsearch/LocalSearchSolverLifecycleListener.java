package org.drools.planner.core.localsearch;

/**
 * @author Geoffrey De Smet
 */
public interface LocalSearchSolverLifecycleListener {

    void solvingStarted(LocalSearchSolverScope localSearchSolverScope);

    void beforeDeciding(StepScope stepScope);

    void stepDecided(StepScope stepScope);

    void stepTaken(StepScope stepScope);

    void solvingEnded(LocalSearchSolverScope localSearchSolverScope);

}
