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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.manager.SolverManager;
import org.optaplanner.core.api.solver.manager.SolverStatus;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSolverManager<Solution_> implements SolverManager<Solution_> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSolverManager.class);

    private ExecutorService solverExecutorService;
    private ExecutorService eventHandlerExecutorService;
    private SolverFactory<Solution_> solverFactory;
    private ConcurrentMap<Object, SolverTask<Solution_>> problemIdToSolverTaskMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Object, List<Future>> problemIdToEventHandlerFuturesMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Object, Consumer<Solution_>> problemIdToOnSolvingEndedMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Object, Consumer<Throwable>> problemIdToOnExceptionMap = new ConcurrentHashMap<>();

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource) {
        Objects.requireNonNull(solverConfigResource);
        return new DefaultSolverManager<>(solverConfigResource, null, null, null);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ClassLoader classLoader) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(classLoader);
        return new DefaultSolverManager<>(solverConfigResource, classLoader, null, null);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ThreadFactory threadFactory) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(threadFactory);
        return new DefaultSolverManager<>(solverConfigResource, null, null, threadFactory);
    }

    public static <Solution_> SolverManager<Solution_> createFromXmlResource(
            String solverConfigResource,
            ClassLoader classLoader,
            ThreadFactory threadFactory) {
        Objects.requireNonNull(solverConfigResource);
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(threadFactory);
        return new DefaultSolverManager<>(solverConfigResource, classLoader, null, threadFactory);
    }

    public static <Solution_> SolverManager<Solution_> createFromSolverFactory(SolverFactory<Solution_> solverFactory) {
        return new DefaultSolverManager<>(null, null, solverFactory, null);
    }

    public static <Solution_> SolverManager<Solution_> createFromSolverFactory(
            SolverFactory<Solution_> solverFactory,
            ThreadFactory threadFactory) {
        Objects.requireNonNull(solverFactory);
        Objects.requireNonNull(threadFactory);
        return new DefaultSolverManager<>(null, null, solverFactory, threadFactory);
    }

    private DefaultSolverManager(String solverConfigResource, ClassLoader classLoader, SolverFactory<Solution_> solverFactory, ThreadFactory threadFactory) {
        if (solverFactory != null) {
            this.solverFactory = solverFactory;
        } else if (classLoader != null) {
            this.solverFactory = SolverFactory.createFromXmlResource(solverConfigResource, classLoader);
        } else {
            this.solverFactory = SolverFactory.createFromXmlResource(solverConfigResource);
        }

        int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
        int activeThreadCount = Math.max(numAvailableProcessors - 2, 1); // #threads < #cpus implies a FIFO scheduling policy
        logger.info("Number of available processors: {}. Active thread count: {}.", numAvailableProcessors, activeThreadCount);

        if (threadFactory != null) {
            this.solverFactory.getSolverConfig().setThreadFactoryClass(threadFactory.getClass());
            solverExecutorService = Executors.newFixedThreadPool(activeThreadCount, threadFactory);
            eventHandlerExecutorService = Executors.newSingleThreadExecutor(threadFactory);
        } else {
            solverExecutorService = Executors.newFixedThreadPool(activeThreadCount);
            eventHandlerExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public UUID solve(Solution_ planningProblem) {
        UUID problemId = UUID.randomUUID();
        solve(problemId, planningProblem);
        return problemId;
    }

    @Override
    public void solve(Object problemId, Solution_ planningProblem) {
        solve(problemId, planningProblem, null, null);
    }

    @Override
    public void solve(
            Object problemId,
            Solution_ planningProblem,
            Consumer<Solution_> onBestSolutionChangedEvent,
            Consumer<Solution_> onSolvingEnded) {
        solve(problemId, planningProblem, onBestSolutionChangedEvent, onSolvingEnded, null);
    }

    @Override
    public void solve(
            Object problemId,
            Solution_ planningProblem,
            Consumer<Solution_> onBestSolutionChangedEvent,
            Consumer<Solution_> onSolvingEnded,
            Consumer<Throwable> onException) {
        Objects.requireNonNull(problemId);
        Objects.requireNonNull(planningProblem);
        SolverTask<Solution_> newSolverTask;
        synchronized (this) {
            if (isProblemSubmitted(problemId)) {
                throw new IllegalArgumentException("Problem (" + problemId + ") already exists.");
            }
            newSolverTask = new SolverTask<>(problemId, solverFactory.buildSolver(), planningProblem);
            problemIdToSolverTaskMap.put(problemId, newSolverTask);
            problemIdToEventHandlerFuturesMap.put(problemId, Collections.synchronizedList(new ArrayList<>()));
            if (onSolvingEnded != null) {
                problemIdToOnSolvingEndedMap.put(problemId, onSolvingEnded);
            }
            if (onException != null) {
                problemIdToOnExceptionMap.put(problemId, onException);
            }

            logger.info("A new solver task was created with problemId ({}).", problemId);
        }

        // TODO implement throttling
        if (onBestSolutionChangedEvent != null) {
            newSolverTask.addEventListener(bestSolutionChangedEvent -> {
                Future<?> bestSolutionChangedEventFuture = eventHandlerExecutorService.submit(
                        () -> onBestSolutionChangedEvent.accept(bestSolutionChangedEvent.getNewBestSolution()));
                problemIdToEventHandlerFuturesMap.get(problemId).add(bestSolutionChangedEventFuture);
            });
        }

        submitSolverTask(newSolverTask);
    }

    private void submitSolverTask(SolverTask<Solution_> solverTask) {
        final Object problemId = solverTask.getProblemId();
        final Consumer<Solution_> onSolvingEnded = problemIdToOnSolvingEndedMap.get(problemId);
        final Consumer<Throwable> onException = problemIdToOnExceptionMap.get(problemId);
        CompletableFuture<Solution_> solverFuture = CompletableFuture.supplyAsync(solverTask::startSolving, solverExecutorService);
        // TODO refactor lambdas in a separate method
        solverFuture.handle((solution_, throwable) -> {
            handleThrowable(throwable, onException, "Exception while solving problem ({" + problemId + "}).");
            if (onSolvingEnded != null) {
                CompletableFuture<Void> solvingEndedFuture = CompletableFuture.supplyAsync(() -> {
                    onSolvingEnded.accept(solution_);
                    return null;
                }, eventHandlerExecutorService);
                // TODO possible NPE if onSolvingEnded is invoked after cleanupResources
                problemIdToEventHandlerFuturesMap.get(problemId).add(solvingEndedFuture);
            }
            return null;
        }).handle((voidCompletableFuture, throwable) -> { // clean-up resources after onSolvingEnded returns
            handleThrowable(throwable, onException, "Exception while executing onSolvingEnded of problem ({" + problemId + "}).");
            return !solverTask.isPaused() && cleanupResources(problemId);
        });
    }

    private void handleThrowable(Throwable throwable, Consumer<Throwable> onException, String message) {
        if (throwable != null) {
            logger.error(message, throwable.getCause());
            if (onException != null) {
                eventHandlerExecutorService.submit(() -> onException.accept(throwable.getCause()));
            }
        }
    }

    @Override
    public boolean terminateSolver(Object problemId) {
        Objects.requireNonNull(problemId);
        logger.debug("Terminating solver of problemId ({}).", problemId);
        synchronized (this) { // should be synchronized to avoid cleaning up resources in onSolvingEnded if it's executed after solverTask.terminateSolver() and before cleanUpResources()
            if (!isProblemSubmitted(problemId)) {
                throw new IllegalArgumentException("Problem (" + problemId + ") was not submitted or finished solving.");
            }
            SolverTask<Solution_> solverTask = problemIdToSolverTaskMap.get(problemId);
            boolean willTerminate = solverTask.stopSolver();
            if (!willTerminate) {
                logger.error("Cannot terminate solver of problemId ({}).", problemId);
                return false;
            }
            // TODO test scenario: submit a problem -> terminate -> re-submit same problem
            //      -> onSolvingEnded for first submission is invoked and might override recent data updates by solving again
            List<Future> problemEventHandlersFutures = problemIdToEventHandlerFuturesMap.get(problemId);
            problemEventHandlersFutures.forEach(future -> future.cancel(false)); // if the event handler is executing (i.e. in the middle of persisting) don't interrupt it
            return cleanupResources(problemId);
        }
    }

    @Override
    public synchronized boolean pauseSolver(Object problemId) {
        logger.debug("Pausing solver of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = getSolverTask(problemId);
        return solverTask != null && solverTask.pauseSolver();
    }

    @Override
    public synchronized boolean resumeSolver(Object problemId) {
        logger.debug("Pausing solver of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = getSolverTask(problemId);
        if (solverTask == null) {
            return false;
        }
        if (!solverTask.isPaused()) {
            logger.error("Solver of problemId ({}) is already solving.", problemId);
            return false;
        }
        submitSolverTask(solverTask);
        return true;
    }

    @Override
    public boolean isProblemSubmitted(Object problemId) {
        Objects.requireNonNull(problemId);
        return problemIdToSolverTaskMap.containsKey(problemId);
    }

    @Override
    public Solution_ getBestSolution(Object problemId) {
        Objects.requireNonNull(problemId);
        logger.debug("Getting best solution of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = getSolverTask(problemId);
        return solverTask == null ? null : solverTask.getBestSolution();
    }

    @Override
    public Score<?> getBestScore(Object problemId) {
        Objects.requireNonNull(problemId);
        logger.debug("Getting best score of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = getSolverTask(problemId);
        return solverTask == null ? null : solverTask.getBestScore();
    }

    @Override
    public SolverStatus getSolverStatus(Object problemId) {
        Objects.requireNonNull(problemId);
        logger.debug("Getting solver status of problemId ({}).", problemId);
        SolverTask<Solution_> solverTask = getSolverTask(problemId);
        return solverTask == null ? null : solverTask.getSolverStatus();
    }

    @Override
    public Map<Object, Indictment> getIndictmentMap(Solution_ solution_) {
        ScoreDirector<Solution_> scoreDirector = solverFactory.buildSolver().getScoreDirectorFactory().buildScoreDirector(); // TODO compare against creating an indictmentMapSolver field
        scoreDirector.setWorkingSolution(solution_);
        scoreDirector.calculateScore();
        return scoreDirector.getIndictmentMap();
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

    private SolverTask<Solution_> getSolverTask(Object problemId) {
        SolverTask<Solution_> solverTask = problemIdToSolverTaskMap.get(problemId);
        if (solverTask == null) {
            logger.error("Problem ({}) was not submitted or finished solving.", problemId);
            return null;
        }
        return solverTask;
    }

    private synchronized boolean cleanupResources(Object problemId) {
        if (isProblemSubmitted(problemId)) {
            problemIdToSolverTaskMap.remove(problemId);
            problemIdToEventHandlerFuturesMap.remove(problemId);
            problemIdToOnSolvingEndedMap.remove(problemId);
            problemIdToOnExceptionMap.remove(problemId);
            problemIdToEventHandlerFuturesMap.remove(problemId);
            return true;
        }
        return false;
    }

    private void stopSolvers() {
        problemIdToSolverTaskMap.values().forEach(SolverTask::stopSolver);
    }
}
