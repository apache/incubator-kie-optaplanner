package org.drools.planner.core.localsearch.decider.deciderscorecomparator;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link DeciderScoreComparatorFactory}.
 * @see DeciderScoreComparatorFactory
 * @author Geoffrey De Smet
 */
public abstract class AbstractDeciderScoreComparatorFactory implements DeciderScoreComparatorFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void beforeDeciding(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepDecided(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepTaken(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

}