package org.drools.solver.core.localsearch.finish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractFinish implements Finish {

    protected final transient Log log = LogFactory.getLog(getClass());

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
