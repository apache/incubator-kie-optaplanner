package org.drools.solver.core.localsearch.decider.accepter;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractAccepter implements Accepter {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted() {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void beforeDeciding() {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepDecided(Move step) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepTaken() {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void solvingEnded() {
        // Hook which can be optionally overwritten by subclasses.
    }

}
