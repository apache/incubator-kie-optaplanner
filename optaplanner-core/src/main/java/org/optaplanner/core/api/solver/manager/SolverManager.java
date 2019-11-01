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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.impl.score.director.ScoreDirector;
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

    // TODO consider an overloaded method for each solve* that returns a UUID
    UUID solveBatch(Solution_ planningProblem,
                    Consumer<Solution_> bestSolutionAtTerminationConsumer);

    void solveBatch(Object problemId,
                    Solution_ planningProblem,
                    Consumer<Solution_> bestSolutionAtTerminationConsumer);

    void solveBatch(Object problemId,
                    Solution_ planningProblem,
                    Consumer<Solution_> bestSolutionAtTerminationConsumer,
                    Consumer<Throwable> onException);

    void solveBestEvents(Object problemId,
                         Solution_ planningProblem,
                         Consumer<Solution_> bestSolutionDuringSolvingConsumer);

    void solveBestEvents(Object problemId,
                         Solution_ planningProblem,
                         Consumer<Solution_> bestSolutionDuringSolvingConsumer,
                         Consumer<Throwable> onException);

    /**
     * Submits a planning problem to be solved and returns immediately. Calling this method creates a new solver job
     * that might start solving immediately or waits until a {@link Thread} is available.
     * To stop a solver job early, call {@link #terminateSolver}.
     * <p>
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
     * Notifies the solver that it should stop at its earliest convenience.
     * Note that after calling this method there will be no reference for problemId in the {@link SolverManager}.
     *
     * @param problemId never null, a unique id for each planning problem
     */
    void terminateSolver(Object problemId);

    /**
     * @param problemId never null, a unique id for each planning problem
     * @return true if a planning problem with id {@param problemId} has already been submitted via {@link #solve}
     */
    boolean isProblemSubmitted(Object problemId);

    // TODO getSolveStatus once SolverStatus is specified

    /**
     * Explains the impact of each planning entity or problem fact on the {@link Score}. See {@link ScoreDirector#getIndictmentMap()}
     *
     * @param solution never null, an instance of {@link PlanningSolution}
     * @return never null, the key is a {@link ProblemFactCollectionProperty problem fact} or a {@link PlanningEntity planning entity}
     */
    Map<Object, Indictment> getIndictmentMap(Solution_ solution);

    /**
     * Terminates all solver jobs (running and awaiting ones) and queued callbacks. No new solver jobs can be submitted
     * after calling this method. An unused {@link SolverManager} should be shut down to allow reclamation of its resources.
     */
    void shutdown();
}
