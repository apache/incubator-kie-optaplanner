/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.api.solver;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.solver.change.ProblemChange;

/**
 * Represents a {@link PlanningSolution problem} that has been submitted to solve on the {@link SolverManager}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
 */
public interface SolverJob<Solution_, ProblemId_> {

    /**
     * @return never null, a value given to {@link SolverManager#solve(Object, Function, Consumer)}
     *         or {@link SolverManager#solveAndListen(Object, Function, Consumer)}
     */
    ProblemId_ getProblemId();

    /**
     * Returns whether the {@link Solver} is scheduled to solve, actively solving or not.
     * <p>
     * Returns {@link SolverStatus#NOT_SOLVING} if the solver already terminated.
     *
     * @return never null
     */
    SolverStatus getSolverStatus();

    // TODO Future features
    //    void reloadProblem(Function<? super ProblemId_, Solution_> problemFinder);

    /**
     * Schedules a {@link ProblemChange} to be processed by the underlying {@link Solver} and returns immediately.
     * <p>
     * To learn more about problem change semantics, please refer to the {@link ProblemChange} Javadoc.
     *
     * @param problemChange never null
     * @return completes after the best solution containing this change has been consumed.
     * @throws IllegalStateException if the underlying {@link Solver} is not in the {@link SolverStatus#SOLVING_ACTIVE}
     *         state
     */
    CompletableFuture<Void> addProblemChange(ProblemChange<Solution_> problemChange);

    /**
     * Terminates the solver or cancels the solver job if it hasn't (re)started yet.
     * <p>
     * Does nothing if the solver already terminated.
     * <p>
     * Waits for the termination or cancellation to complete before returning.
     * During termination, a {@code bestSolutionConsumer} could still be called. When the solver terminates,
     * the {@code finalBestSolutionConsumer} is executed with the latest best solution.
     * These consumers run on a consumer thread independently of the termination and may still run even after
     * this method returns.
     */
    void terminateEarly();

    /**
     * @return true if {@link SolverJob#terminateEarly} has been called since the underlying {@link Solver}
     *         started solving.
     */
    boolean isTerminatedEarly();

    /**
     * Waits if necessary for the solver to complete and then returns the final best {@link PlanningSolution}.
     *
     * @return never null, but it could be the original uninitialized problem
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     */
    Solution_ getFinalBestSolution() throws InterruptedException, ExecutionException;

    /**
     * Returns the {@link Duration} spent solving since the last start.
     * If it hasn't started it yet, it returns {@link Duration#ZERO}.
     * If it hasn't ended yet, it returns the time between the last start and now.
     * If it has ended already, it returns the time between the last start and the ending.
     *
     * @return the {@link Duration} spent solving since the last (re)start, at least 0
     */
    Duration getSolvingDuration();

}
