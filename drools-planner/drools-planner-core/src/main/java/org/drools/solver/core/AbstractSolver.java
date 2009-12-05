package org.drools.solver.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.solver.core.localsearch.DefaultLocalSearchSolver;
import org.drools.solver.core.event.SolverEventSupport;
import org.drools.solver.core.event.SolverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link Solver}.
 * @see Solver
 * @see DefaultLocalSearchSolver
 * @author Geoffrey De Smet
 */
public abstract class AbstractSolver implements Solver {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected SolverEventSupport solverEventSupport = new SolverEventSupport(this);

    // TODO atomic enum with values NOT_STARTED, RUNNING, DONE, CANCELLED
    // TODO introduce a solver factory and make a solver one time use
    protected final AtomicBoolean terminatedEarly = new AtomicBoolean(false);

    public boolean terminateEarly() {
        boolean terminationEarlySuccessful = !terminatedEarly.getAndSet(true);
        if (terminationEarlySuccessful) {
            logger.info("Terminating solver early.");
        }
        return terminationEarlySuccessful;
    }

    public boolean isTerminatedEarly() {
        return terminatedEarly.get();
    }

    public final void solve() {
        terminatedEarly.set(false);
        solveImplementation();
    }

    protected abstract void solveImplementation();

    public void addEventListener(SolverEventListener eventListener) {
        solverEventSupport.addEventListener(eventListener);
    }

    public void removeEventListener(SolverEventListener eventListener) {
        solverEventSupport.removeEventListener(eventListener);
    }

}
