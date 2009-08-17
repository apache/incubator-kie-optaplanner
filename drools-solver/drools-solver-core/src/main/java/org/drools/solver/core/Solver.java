package org.drools.solver.core;

import java.util.concurrent.Future;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;

/**
 * A Solver solves planning problems.
 * <p/>
 * Most methods are not thread-safe and should be called from the same thread.
 * @author Geoffrey De Smet
 */
public interface Solver {

    /**
     * @param startingSolution never null
     */
    void setStartingSolution(Solution startingSolution);
    
    Solution getBestSolution();

    /**
     * @return the amount of millis spend since this solver started
     */
    long getTimeMillisSpend();

    /**
     * Solves the planning problem.
     * It can take minutes, even hours or days before this method returns,
     * depending on the termination configuration.
     * To terminate a {@link Solver} early, call {@link #terminateEarly()}.
     * @see #terminateEarly()
     */
    void solve();

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * This method returns immediatly, but it takes an undetermined time
     * for the {@link #solve()} to actually return.
     * <p/>
     * This method is thread-safe.
     * @see #isTerminatedEarly()
     * @see Future#cancel(boolean)
     * @return true if successful
     */
    boolean terminateEarly();

    /**
     * This method is thread-safe.
     * @see Future#isCancelled()
     * @return true if terminateEarly has been called since the {@Solver} started.
     */
    boolean isTerminatedEarly();

}
