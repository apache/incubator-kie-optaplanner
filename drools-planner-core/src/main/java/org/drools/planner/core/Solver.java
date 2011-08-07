/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.solver.PlanningFactChange;
import org.drools.planner.core.termination.Termination;

/**
 * A Solver solves planning problems.
 * <p/>
 * Most methods are not thread-safe and should be called from the same thread.
 */
public interface Solver {

    /**
     * @param startingSolution never null
     */
    void setStartingSolution(Solution startingSolution);

    /**
     * @return never null after solving
     */
    Solution getBestSolution();

    /**
     * TODO timeMillisSpend should not continue to increase after the solver has been terminated
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
     * This method is thread-safe.
     * @return true if the {@link #solve()} method is still running.
     */
    boolean isSolving();

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * This method returns immediately, but it takes an undetermined time
     * for the {@link #solve()} to actually return.
     * <p/>
     * This method is thread-safe.
     * @return true if successful
     * @see #isTerminateEarly()
     * @see Future#cancel(boolean)
     */
    boolean terminateEarly();

    /**
     * This method is thread-safe.
     * @return true if terminateEarly has been called since the {@Solver} started.
     * @see Future#isCancelled()
     */
    boolean isTerminateEarly();

    /**
     * Schedules a {@link PlanningFactChange} to be processed.
     * <p/>
     * As a side-effect this restarts the {@link Solver}, effectively resetting all {@link Termination}s,
     * but not {@link #terminateEarly()}.
     * <p/>
     * Follows specifications of {@link BlockingQueue#add(Object)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     * @param planningFactChange never null
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    boolean addPlanningFactChange(PlanningFactChange planningFactChange);

    /**
     * @param eventListener never null
     */
    void addEventListener(SolverEventListener eventListener);

    /**
     * @param eventListener never null
     */
    void removeEventListener(SolverEventListener eventListener);

    /**
     * @return never null
     */
    ScoreDefinition getScoreDefinition();

}
