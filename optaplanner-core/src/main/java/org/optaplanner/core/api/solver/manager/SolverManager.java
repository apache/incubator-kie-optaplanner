/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.solver.manager;

import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.impl.solver.manager.DefaultSolverManager;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * A SolverManager solves multiple planning problems of the same domain at the same time. It also manages the thread pool
 * with the internal solver threads.
 * Clients usually call {@link #solve} with a {@link BestSolutionChangedEvent} handler.
 * <p>
 * These methods are thread-safe
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public interface SolverManager<Solution_> extends AutoCloseable {

    // ************************************************************************
    // Static creation methods:
    // ************************************************************************

    /**
     * @param solverConfigResource never null, a classpath resource
     * @param <Solution_>          the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource) {
        return DefaultSolverManager.createFromXmlResource(solverConfigResource);
    }

    /**
     * @param solverConfigResource never null, a classpath resource as defined by {@link ClassLoader#getResource(String)}
     * @param classLoader          never null, the {@link ClassLoader} to use for loading all resources and {@link Class}es,
     *                             to use the default {@link ClassLoader} call {@link #createFromXmlResource(String)}
     * @param <Solution_>          the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ClassLoader classLoader) {
        return DefaultSolverManager.createFromXmlResource(solverConfigResource, classLoader);
    }

    /**
     * @param solverConfigResource never null, a classpath resource as defined by {@link ClassLoader#getResource(String)}
     * @param threadFactory        never null, a custom {@link ThreadFactory} for thread creation, to use the default
     *                             {@link ThreadFactory} call {@link #createFromXmlResource(String)}
     * @param <Solution_>          the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ThreadFactory threadFactory) {
        return DefaultSolverManager.createFromXmlResource(solverConfigResource, threadFactory);
    }

    /**
     * @param solverConfigResource never null, a classpath resource as defined by {@link ClassLoader#getResource(String)}
     * @param classLoader          never null, the {@link ClassLoader} to use for loading all resources and {@link Class}es
     * @param threadFactory        never null, a custom {@link ThreadFactory} for thread creation
     * @param <Solution_>          the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromXmlResource(
            String solverConfigResource,
            ClassLoader classLoader,
            ThreadFactory threadFactory) {
        return DefaultSolverManager.createFromXmlResource(solverConfigResource, classLoader, threadFactory);
    }

    /**
     * @param solverFactory never null, a {@link SolverFactory}
     * @param <Solution_>   the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromSolverFactory(SolverFactory<Solution_> solverFactory) {
        return DefaultSolverManager.createFromSolverFactory(solverFactory);
    }

    /**
     * @param solverFactory never null, a {@link SolverFactory}
     * @param threadFactory never null, a custom {@link ThreadFactory} for thread creation, to use the default
     *                      {@link ThreadFactory} call {@link #createFromSolverFactory(SolverFactory)}
     * @param <Solution_>   the solution type, the class with the {@link PlanningSolution} annotation
     * @return never null
     */
    static <Solution_> SolverManager<Solution_> createFromSolverFactory(
            SolverFactory<Solution_> solverFactory,
            ThreadFactory threadFactory) {
        return DefaultSolverManager.createFromSolverFactory(solverFactory, threadFactory);
    }

    // ************************************************************************
    // Interface methods
    // ************************************************************************

    /**
     * Submits a planning problem to be solved and returns immediately. Calling this method creates a new solver job
     * that might start solving immediately or waits until a {@link Thread} is available.
     * To stop a solver job early, call {@link #terminateSolver}.
     * <p>
     * To get the current best solution (which might or might not be optimal, feasible or even initialized),
     * call {@link #getBestSolution}.
     *
     * @param planningProblem never null, an instance of {@link PlanningSolution}, usually its planning variables are uninitialized
     * @return a Universally Unique Identifier "UUID" for the submitted problem
     */
    UUID solve(Solution_ planningProblem);

    /**
     * Submits a planning problem to be solved and returns immediately. Calling this method creates a new solver job
     * that might start solving immediately or waits until a {@link Thread} is available.
     * To stop a solver job early, call {@link #terminateSolver}.
     * <p>
     * To get the current best solution (which might or might not be optimal, feasible or even initialized),
     * call {@link #getBestSolution}.
     *
     * @param problemId       never null, a unique id for each planning problem
     * @param planningProblem never null, an instance of {@link PlanningSolution}, usually its planning variables are uninitialized
     */

    void solve(Object problemId, Solution_ planningProblem);

    /**
     * Submits a planning problem to be solved and returns immediately. Calling this method creates a new solver job
     * that might start solving immediately or waits until a {@link Thread} is available.
     * To stop a solver job early, call {@link #terminateSolver}.
     * <p>
     * To get the current best solution (which might or might not be optimal, feasible or even initialized),
     * call {@link #getBestSolution}.
     *
     * @param problemId                  never null, a unique id for each planning problem
     * @param planningProblem            never null, an instance of {@link PlanningSolution}, usually its planning variables are uninitialized
     * @param onBestSolutionChangedEvent sometimes null, an event handler for {@link BestSolutionChangedEvent}
     * @param onSolvingEnded             sometimes null, a method executed when the solver job is done with the best solution encountered
     *                                   (which might or might not be optimal, feasible or even initialized). It can take seconds, minutes,
     *                                   even hours or days before this method is invoked, depending on the {@link Termination} configuration and
     *                                   scheduling of the solver job
     */
    void solve(
            Object problemId,
            Solution_ planningProblem,
            Consumer<Solution_> onBestSolutionChangedEvent,
            Consumer<Solution_> onSolvingEnded);

    /**
     * Submits a planning problem to be solved and returns immediately. Calling this method creates a new solver job
     * that might start solving immediately or waits until a {@link Thread} is available.
     * To stop a solver job early, call {@link #terminateSolver}.
     * <p>
     * To get the current best solution (which might or might not be optimal, feasible or even initialized),
     * call {@link #getBestSolution}.
     *
     * @param problemId                  never null, a unique id for each planning problem
     * @param planningProblem            never null, an instance of {@link PlanningSolution}, usually its planning variables are uninitialized
     * @param onBestSolutionChangedEvent sometimes null, an event handler for {@link BestSolutionChangedEvent}
     * @param onSolvingEnded             sometimes null, a callback executed when the solver job is done with the best solution encountered
     *                                   (which might or might not be optimal, feasible or even initialized). It can take seconds, minutes,
     *                                   even hours or days before this method is invoked, depending on the {@link Termination} configuration and
     *                                   scheduling of the solver job
     * @param onException                sometimes null, a callback invoked if the solver job throws an exception
     */
    void solve(
            Object problemId,
            Solution_ planningProblem,
            Consumer<Solution_> onBestSolutionChangedEvent,
            Consumer<Solution_> onSolvingEnded,
            Consumer<Throwable> onException);

    /**
     * Notifies the solver that it should stop at its earliest convenience and clean up all the resources used by the
     * corresponding solver task. Note that after calling this method there will be no reference for problemId in the
     * {@link SolverManager}.
     *
     * @param problemId never null, a unique id for each planning problem
     * @return true if successful, false if no problem with problemId has been submitted
     */
    boolean terminateSolver(Object problemId);

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * To resume solving call {@link #resumeSolver(Object problemId)}
     *
     * @param problemId never null, a unique id for each planning problem
     * @return true if successful, false if no problem with problemId has been submitted
     */
    boolean pauseSolver(Object problemId);

    /**
     * Resumes solving a previously stopped solver task.
     *
     * @param problemId never null, a unique id for each planning problem
     * @return true if successful, false if no problem with problemId has been submitted or solver is already solving
     */
    boolean resumeSolver(Object problemId);

    /**
     * @param problemId never null, a unique id for each planning problem
     * @return true if a planning problem with id {@param problemId} has already been submitted via {@link #solve}
     */
    boolean isProblemSubmitted(Object problemId);

    /**
     * The best solution is the {@link PlanningSolution best solution} found during solving:
     * it might or might not be optimal, feasible or even initialized.
     * <p>
     * This method is useful in rare asynchronous situations (although {@code onBestSolutionChangedEvent} callback
     * of {@link #solve} is often more appropriate).
     *
     * @param problemId never null, a unique id for each planning problem
     * @return null if the problemId isn't associated with a previously submitted problem, or
     * {@link PlanningSolution best solution} which might be uninitialized with a null {@link Score} if the solver job
     * has not started yet
     */
    Solution_ getBestSolution(Object problemId);

    /**
     * Returns the {@link Score} of the {@link #getBestSolution}.
     * <p>
     * This is useful for generic code, which doesn't know the type of the {@link PlanningSolution}
     * to retrieve the {@link Score} from the {@link #getBestSolution} easily.
     *
     * @param problemId never null, a unique id for each planning problem
     * @return null if the problemId isn't associated with a previously submitted problem, the solver job has not started yet
     */
    Score<?> getBestScore(Object problemId);

    /**
     * @param problemId never null, a unique id for each planning problem
     * @return null if the problemId isn't associated with a previously submitted problem, {@link SolverStatus} otherwise
     */
    SolverStatus getSolverStatus(Object problemId);

    /**
     * Terminates all solver jobs (running and awaiting ones) and queued callbacks. No new solver jobs can be submitted
     * after calling this method. An unused {@link SolverManager} should be shut down to allow reclamation of its resources.
     */
    void shutdown();
}
