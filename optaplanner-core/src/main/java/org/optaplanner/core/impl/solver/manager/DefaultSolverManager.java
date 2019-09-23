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

package org.optaplanner.core.impl.solver.manager;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.manager.SolverManager;
import org.optaplanner.core.api.solver.manager.SolverStatus;
import org.optaplanner.core.impl.solver.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSolverManager<Solution_> implements SolverManager<Solution_> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSolverManager.class);

    private ExecutorService solverExecutorService;
    private ExecutorService eventHandlerExecutorService;
    private SolverFactory<Solution_> solverFactory;
    private ConcurrentMap<Object, SolverTask<Solution_>> problemIdToSolverTaskMap;

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource) {
        Objects.requireNonNull(solverConfigResource);
        return new DefaultSolverManager<>(solverConfigResource, null, null);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ClassLoader classLoader) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(classLoader);
        return new DefaultSolverManager<>(solverConfigResource, classLoader, null);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ThreadFactory threadFactory) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(threadFactory);
        return new DefaultSolverManager<>(solverConfigResource, null, threadFactory);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource,
                                                                             ClassLoader classLoader, ThreadFactory threadFactory) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(threadFactory);
        return new DefaultSolverManager<>(solverConfigResource, classLoader, threadFactory);
    }

    private DefaultSolverManager(String solverConfigResource, ClassLoader classLoader, ThreadFactory threadFactory) {
        if (classLoader != null) {
            solverFactory = SolverFactory.createFromXmlResource(solverConfigResource, classLoader);
        } else {
            solverFactory = SolverFactory.createFromXmlResource(solverConfigResource);
        }

        problemIdToSolverTaskMap = new ConcurrentHashMap<>();
        int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
        int activeThreadCount = Math.max(numAvailableProcessors - 2, 1);
        logger.info("Number of available processors: {}. Active thread count: {}.", numAvailableProcessors, activeThreadCount);

        if (threadFactory != null) {
            solverFactory.getSolverConfig().setThreadFactoryClass(threadFactory.getClass());
            solverExecutorService = Executors.newFixedThreadPool(activeThreadCount, threadFactory); // #threads < #cpus implies a FIFO scheduling policy
            eventHandlerExecutorService = Executors.newSingleThreadExecutor(threadFactory);
        } else {
            solverExecutorService = Executors.newFixedThreadPool(numAvailableProcessors - 1);
            eventHandlerExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void solve(Object problemId,
                      Solution_ planningProblem,
                      Consumer<Solution_> onBestSolutionChangedEvent,
                      Consumer<Solution_> onSolvingEnded) {
        solve(problemId, planningProblem, onBestSolutionChangedEvent, onSolvingEnded, null);
    }

    @Override
    public void solve(Object problemId,
                      Solution_ planningProblem,
                      Consumer<Solution_> onBestSolutionChangedEvent,
                      Consumer<Solution_> onSolvingEnded,
                      Consumer<Throwable> onException) {
        SolverTask<Solution_> newSolverTask;
        synchronized (this) {
            if (isProblemSubmitted(problemId)) {
                throw new IllegalArgumentException("Problem (" + problemId + ") already exists.");
            }
            newSolverTask = new SolverTask<>(problemId, solverFactory.buildSolver(), planningProblem);
            problemIdToSolverTaskMap.put(problemId, newSolverTask);
            logger.info("A new solver task was created with problemId ({}).", problemId);
        }

        // TODO implement throttling
        if (onBestSolutionChangedEvent != null) {
            newSolverTask.addEventListener(
                    bestSolutionChangedEvent ->
                            eventHandlerExecutorService.submit(
                                    () -> onBestSolutionChangedEvent.accept(bestSolutionChangedEvent.getNewBestSolution())));
        }

        CompletableFuture<Solution_> solverFuture = CompletableFuture.supplyAsync(newSolverTask::startSolving, solverExecutorService);
        solverFuture.handle((solution_, throwable) -> {
            if (throwable != null) {
                logger.error("Exception while solving problem ({}).", problemId, throwable.getCause());
                if (onException != null) {
                    eventHandlerExecutorService.submit(() -> onException.accept(throwable.getCause()));
                }
            }
            return eventHandlerExecutorService.submit(() -> onSolvingEnded.accept(solution_));
        });
    }

    @Override
    public void stopSolver(Object problemId) {
        logger.debug("Stopping solver of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask;
        synchronized (this) {
            if (!isProblemSubmitted(problemId)) {
                throw new IllegalArgumentException("Problem (" + problemId + ") was not submitted.");
            }
            solverTask = problemIdToSolverTaskMap.get(problemId);
        }
        if (solverTask != null) {
            solverTask.stopSolver();
        }
    }

    @Override
    public boolean isProblemSubmitted(Object problemId) {
        return problemIdToSolverTaskMap.containsKey(problemId);
    }

    @Override
    public Solution_ getBestSolution(Object problemId) {
        logger.debug("Getting best solution of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = problemIdToSolverTaskMap.get(problemId);
        if (solverTask == null) {
            logger.error("Problem ({}) was not submitted.", problemId);
            return null;
        }
        return solverTask.getBestSolution();
    }

    @Override
    public Score<?> getBestScore(Object problemId) {
        logger.debug("Getting best score of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = problemIdToSolverTaskMap.get(problemId);
        if (solverTask == null) {
            logger.error("Problem ({}) was not submitted.", problemId);
            return null;
        }
        return solverTask.getBestScore();
    }

    @Override
    public SolverStatus getSolverStatus(Object problemId) {
        logger.debug("Getting solver status of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = problemIdToSolverTaskMap.get(problemId);
        if (solverTask == null) {
            logger.error("Problem ({}) was not submitted.", problemId);
            return null;
        }
        return solverTask.getSolverStatus();
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down {}.", DefaultSolverManager.class.getName());
        ThreadUtils.shutdownAwaitOrKill(solverExecutorService, "", "solverExecutorService");
        ThreadUtils.shutdownAwaitOrKill(eventHandlerExecutorService, "", "eventHandlerExecutorService");
        stopSolvers(); // without this: shutdownAwaitOrKill appears to only interrupt currently running tasks, those that are queued start solving
    }

    private void stopSolvers() {
        for (SolverTask<Solution_> solverTask : problemIdToSolverTaskMap.values()) {
            solverTask.stopSolver();
        }
    }
}
