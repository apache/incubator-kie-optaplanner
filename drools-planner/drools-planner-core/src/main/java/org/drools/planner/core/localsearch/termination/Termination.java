package org.drools.planner.core.localsearch.termination;

import org.drools.planner.core.localsearch.LocalSearchSolverAware;
import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.StepScope;

/**
 * A Termination determines when the LocalSearchSolver should stop.
 * @author Geoffrey De Smet
 */
public interface Termination extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    /**
     * Called by the LocalSearchSolver after every step to determine if the search should stop.
     * @param stepScope never null
     * @return true if the search should terminate.
     */
    boolean isTerminated(StepScope stepScope);

    /**
     * A timeGradient is a relative estimate of how long the search will continue.
     * </p>
     * Clients that use a timeGradient should cache it at the start of a single step
     * because some implementations are not time-stable.
     * </p>
     * If a timeGradient can not be calulated, it should return -1.0.
     * Several implementations (such a similated annealing) require a correctly implemented timeGradient.
     * <p/>
     * A Termination's timeGradient can be requested after they are terminated, so implementations
     * should be carefull not to return a tempature above 1.0.
     * @param stepScope never null
     * @return timeGradient t for which 0.0 &lt;= t &lt;= 1.0 or -1.0 when it is not supported.
     * At the start of a search t is 0.0 and at the end of a search t would be 1.0.
     */
    double calculateTimeGradient(StepScope stepScope);

}
