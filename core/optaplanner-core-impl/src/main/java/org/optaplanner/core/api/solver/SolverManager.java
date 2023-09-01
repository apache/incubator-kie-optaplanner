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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.impl.solver.DefaultSolverManager;

/**
 * A SolverManager solves multiple planning problems of the same domain,
 * asynchronously without blocking the calling thread.
 * <p>
 * To create a SolverManager, use {@link #create(SolverFactory, SolverManagerConfig)}.
 * To solve a planning problem, call {@link #solve(Object, Function, Consumer)}
 * or {@link #solveAndListen(Object, Function, Consumer)}.
 * <p>
 * These methods are thread-safe unless explicitly stated otherwise.
 * <p>
 * Internally a SolverManager manages a thread pool of solver threads (which call {@link Solver#solve(Object)})
 * and consumer threads (to handle the {@link BestSolutionChangedEvent}s).
 * <p>
 * To learn more about problem change semantics, please refer to the {@link ProblemChange} Javadoc.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
 */
public interface SolverManager<Solution_, ProblemId_> extends AutoCloseable {

    // ************************************************************************
    // Static creation methods: SolverConfig and SolverFactory
    // ************************************************************************

    /**
     * Use a {@link SolverConfig} to build a {@link SolverManager}.
     * <p>
     * When using {@link SolutionManager} too, use {@link #create(SolverFactory)} instead
     * so they reuse the same {@link SolverFactory} instance.
     *
     * @param solverConfig never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}
     */
    static <Solution_, ProblemId_> SolverManager<Solution_, ProblemId_> create(
            SolverConfig solverConfig) {
        return create(solverConfig, new SolverManagerConfig());
    }

    /**
     * Use a {@link SolverConfig} and a {@link SolverManagerConfig} to build a {@link SolverManager}.
     * <p>
     * When using {@link SolutionManager} too, use {@link #create(SolverFactory, SolverManagerConfig)} instead
     * so they reuse the same {@link SolverFactory} instance.
     *
     * @param solverConfig never null
     * @param solverManagerConfig never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
     */
    static <Solution_, ProblemId_> SolverManager<Solution_, ProblemId_> create(
            SolverConfig solverConfig, SolverManagerConfig solverManagerConfig) {
        return create(SolverFactory.create(solverConfig), solverManagerConfig);
    }

    /**
     * Use a {@link SolverFactory} to build a {@link SolverManager}.
     *
     * @param solverFactory never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}
     */
    static <Solution_, ProblemId_> SolverManager<Solution_, ProblemId_> create(
            SolverFactory<Solution_> solverFactory) {
        return create(solverFactory, new SolverManagerConfig());
    }

    /**
     * Use a {@link SolverFactory} and a {@link SolverManagerConfig} to build a {@link SolverManager}.
     *
     * @param solverFactory never null
     * @param solverManagerConfig never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
     */
    static <Solution_, ProblemId_> SolverManager<Solution_, ProblemId_> create(
            SolverFactory<Solution_> solverFactory, SolverManagerConfig solverManagerConfig) {
        return new DefaultSolverManager<>(solverFactory, solverManagerConfig);
    }

    // ************************************************************************
    // Interface methods
    // ************************************************************************

    /**
     * Submits a planning problem to solve and returns immediately.
     * The planning problem is solved on a solver {@link Thread}, as soon as one is available.
     * To retrieve the final best solution, use {@link SolverJob#getFinalBestSolution()}.
     * <p>
     * In server applications, it's recommended to use {@link #solve(Object, Function, Consumer)} instead,
     * to avoid loading the problem going stale if solving can't start immediately.
     * To listen to intermediate best solutions too, use {@link #solveAndListen(Object, Function, Consumer)} instead.
     * <p>
     * Defaults to logging exceptions as an error.
     * <p>
     * To stop a solver job before it naturally terminates, call {@link SolverJob#terminateEarly()}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     * @param problem never null, a {@link PlanningSolution} usually with uninitialized planning variables
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solve(ProblemId_ problemId, Solution_ problem) {
        return solve(problemId, (problemId_) -> problem, null, null);
    }

    /**
     * As defined by {@link #solve(Object, Object)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problem never null, a {@link PlanningSolution} usually with uninitialized planning variables
     * @param finalBestSolutionConsumer sometimes null, called only once, at the end, on a consumer thread
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solve(ProblemId_ problemId,
            Solution_ problem, Consumer<? super Solution_> finalBestSolutionConsumer) {
        return solve(problemId, (problemId_) -> problem, finalBestSolutionConsumer, null);
    }

    /**
     * As defined by {@link #solve(Object, Object)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problem never null, a {@link PlanningSolution} usually with uninitialized planning variables
     * @param finalBestSolutionConsumer sometimes null, called only once, at the end, on a consumer thread
     * @param exceptionHandler sometimes null, called if an exception or error occurs.
     *        If null it defaults to logging the exception as an error.
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solve(ProblemId_ problemId,
            Solution_ problem, Consumer<? super Solution_> finalBestSolutionConsumer,
            BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler) {
        return solve(problemId, (problemId_) -> problem, finalBestSolutionConsumer, exceptionHandler);
    }

    /**
     * Submits a planning problem to solve and returns immediately.
     * The planning problem is solved on a solver {@link Thread}, as soon as one is available.
     * <p>
     * When the solver terminates, the {@code finalBestSolutionConsumer} is called once with the final best solution,
     * on a consumer {@link Thread}, as soon as one is available.
     * To listen to intermediate best solutions too, use {@link #solveAndListen(Object, Function, Consumer)} instead.
     * <p>
     * Defaults to logging exceptions as an error.
     * <p>
     * To stop a solver job before it naturally terminates, call {@link #terminateEarly(Object)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problemFinder never null, a function that returns a {@link PlanningSolution}, usually with uninitialized planning
     *        variables
     * @param finalBestSolutionConsumer sometimes null, called only once, at the end, on a consumer thread
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solve(ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder,
            Consumer<? super Solution_> finalBestSolutionConsumer) {
        return solve(problemId, problemFinder, finalBestSolutionConsumer, null);
    }

    /**
     * As defined by {@link #solve(Object, Function, Consumer)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problemFinder never null, function that returns a {@link PlanningSolution}, usually with uninitialized planning
     *        variables
     * @param finalBestSolutionConsumer sometimes null, called only once, at the end, on a consumer thread
     * @param exceptionHandler sometimes null, called if an exception or error occurs.
     *        If null it defaults to logging the exception as an error.
     * @return never null
     */
    SolverJob<Solution_, ProblemId_> solve(ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder,
            Consumer<? super Solution_> finalBestSolutionConsumer,
            BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler);

    /**
     * Submits a planning problem to solve and returns immediately.
     * The planning problem is solved on a solver {@link Thread}, as soon as one is available.
     * <p>
     * When the solver finds a new best solution, the {@code bestSolutionConsumer} is called every time,
     * on a consumer {@link Thread}, as soon as one is available (taking into account any throttling waiting time),
     * unless a newer best solution is already available by then (in which case skip ahead discards it).
     * <p>
     * Defaults to logging exceptions as an error.
     * <p>
     * To stop a solver job before it naturally terminates, call {@link #terminateEarly(Object)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problemFinder never null, a function that returns a {@link PlanningSolution}, usually with uninitialized planning
     *        variables
     * @param bestSolutionConsumer never null, called multiple times, on a consumer thread
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solveAndListen(ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder, Consumer<? super Solution_> bestSolutionConsumer) {
        return solveAndListen(problemId, problemFinder, bestSolutionConsumer, null, null);
    }

    /**
     * As defined by {@link #solveAndListen(Object, Function, Consumer)}.
     *
     * @param problemId never null, a ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problemFinder never null, function that returns a {@link PlanningSolution}, usually with uninitialized planning
     *        variables
     * @param bestSolutionConsumer never null, called multiple times, on a consumer thread
     * @param exceptionHandler sometimes null, called if an exception or error occurs.
     *        If null it defaults to logging the exception as an error.
     * @return never null
     */
    default SolverJob<Solution_, ProblemId_> solveAndListen(ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder,
            Consumer<? super Solution_> bestSolutionConsumer,
            BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler) {
        return solveAndListen(problemId, problemFinder, bestSolutionConsumer, null, exceptionHandler);
    }

    /**
     * As defined by {@link #solveAndListen(Object, Function, Consumer)}.
     * <p>
     * The final best solution is delivered twice:
     * first to the {@code bestSolutionConsumer} when it is found
     * and then again to the {@code finalBestSolutionConsumer} when the solver terminates.
     * Do not store the solution twice.
     * This allows for use cases that only process the {@link Score} first (during best solution changed events)
     * and then store the solution upon termination.
     *
     * @param problemId never null, an ID for each planning problem. This must be unique.
     *        Use this problemId to {@link #terminateEarly(Object) terminate} the solver early,
     *        {@link #getSolverStatus(Object) to get the status} or if the problem changes while solving.
     * @param problemFinder never null, function that returns a {@link PlanningSolution}, usually with uninitialized planning
     *        variables
     * @param bestSolutionConsumer never null, called multiple times, on a consumer thread
     * @param finalBestSolutionConsumer sometimes null, called only once, at the end, on a consumer thread.
     *        That final best solution is already consumed by the bestSolutionConsumer earlier.
     * @param exceptionHandler sometimes null, called if an exception or error occurs.
     *        If null it defaults to logging the exception as an error.
     * @return never null
     */
    SolverJob<Solution_, ProblemId_> solveAndListen(ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder,
            Consumer<? super Solution_> bestSolutionConsumer,
            Consumer<? super Solution_> finalBestSolutionConsumer,
            BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler);

    /**
     * Returns if the {@link Solver} is scheduled to solve, actively solving or not.
     * <p>
     * Returns {@link SolverStatus#NOT_SOLVING} if the solver already terminated or if the problemId was never added.
     * To distinguish between both cases, use {@link SolverJob#getSolverStatus()} instead.
     * Here, that distinction is not supported because it would cause a memory leak.
     *
     * @param problemId never null, a value given to {@link #solve(Object, Function, Consumer)}
     *        or {@link #solveAndListen(Object, Function, Consumer)}
     * @return never null
     */
    SolverStatus getSolverStatus(ProblemId_ problemId);

    // TODO Future features
    //    void reloadProblem(ProblemId_ problemId, Function<? super ProblemId_, Solution_> problemFinder);

    /**
     * Schedules a {@link ProblemChange} to be processed by the underlying {@link Solver} and returns immediately.
     * If the solver already terminated or the problemId was never added, throws an exception.
     * The same applies if the underlying {@link Solver} is not in the {@link SolverStatus#SOLVING_ACTIVE} state.
     *
     * @param problemId never null, a value given to {@link #solve(Object, Function, Consumer)}
     *        or {@link #solveAndListen(Object, Function, Consumer)}
     * @param problemChange never null
     * @return completes after the best solution containing this change has been consumed.
     * @throws IllegalStateException if there is no solver actively solving the problem associated with the problemId
     */
    CompletableFuture<Void> addProblemChange(ProblemId_ problemId, ProblemChange<Solution_> problemChange);

    /**
     * Terminates the solver or cancels the solver job if it hasn't (re)started yet.
     * <p>
     * Does nothing if the solver already terminated or the problemId was never added.
     * To distinguish between both cases, use {@link SolverJob#terminateEarly()} instead.
     * Here, that distinction is not supported because it would cause a memory leak.
     * <p>
     * Waits for the termination or cancellation to complete before returning.
     * During termination, a {@code bestSolutionConsumer} could still be called. When the solver terminates,
     * the {@code finalBestSolutionConsumer} is executed with the latest best solution.
     * These consumers run on a consumer thread independently of the termination and may still run even after
     * this method returns.
     *
     * @param problemId never null, a value given to {@link #solve(Object, Function, Consumer)}
     *        or {@link #solveAndListen(Object, Function, Consumer)}
     */
    void terminateEarly(ProblemId_ problemId);

    /**
     * Terminates all solvers, cancels all solver jobs that haven't (re)started yet
     * and discards all queued {@link ProblemChange}s.
     * Releases all thread pool resources.
     * <p>
     * No new planning problems can be submitted after calling this method.
     */
    @Override
    void close();

}
