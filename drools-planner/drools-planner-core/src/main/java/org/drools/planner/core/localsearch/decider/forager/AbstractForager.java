package org.drools.planner.core.localsearch.decider.forager;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link Forager}.
 * @see Forager
 * @author Geoffrey De Smet
 */
public abstract class AbstractForager implements Forager {

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
