package org.drools.solver.core.localsearch.decider.selector;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.Decider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link Selector}.
 * @see Selector
 * @author Geoffrey De Smet
 */
public abstract class AbstractSelector implements Selector {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Decider decider;

    public void setDecider(Decider decider) {
        this.decider = decider;
    }

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