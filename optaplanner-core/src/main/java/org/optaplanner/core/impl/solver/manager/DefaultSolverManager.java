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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.manager.SolverManager;
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
    private ConcurrentMap<Object, List<Future>> problemIdToFuturesMap = new ConcurrentHashMap<>();

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
    public UUID solveBatch(Solution_ solution_, Consumer<Solution_> bestSolutionAtTerminationConsumer) {
        UUID problemId = UUID.randomUUID();
        solveBatch(problemId, solution_, bestSolutionAtTerminationConsumer);
        return problemId;
    }

    @Override
    public void solveBatch(Object problemId, Solution_ planningProblem, Consumer<Solution_> bestSolutionAtTerminationConsumer) {
        solveBatch(problemId, planningProblem, bestSolutionAtTerminationConsumer, null);
    }

    @Override
    public void solveBatch(Object problemId, Solution_ planningProblem, Consumer<Solution_> bestSolutionAtTerminationConsumer, Consumer<Throwable> onException) {
        Objects.requireNonNull(bestSolutionAtTerminationConsumer);
        solve(problemId, planningProblem, null, bestSolutionAtTerminationConsumer, onException);
    }

    @Override
    public void solveBestEvents(Object problemId, Solution_ planningProblem, Consumer<Solution_> bestSolutionDuringSolvingConsumer) {
        solveBestEvents(problemId, planningProblem, bestSolutionDuringSolvingConsumer, null);
    }

    @Override
    public void solveBestEvents(Object problemId, Solution_ planningProblem, Consumer<Solution_> bestSolutionDuringSolvingConsumer, Consumer<Throwable> onException) {
        Objects.requireNonNull(bestSolutionDuringSolvingConsumer);
        solve(problemId, planningProblem, bestSolutionDuringSolvingConsumer, null, onException);
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
            problemIdToFuturesMap.put(problemId, new CopyOnWriteArrayList<>());
            logger.info("A new solver task was created with problemId ({}).", problemId);
        }

        // TODO implement throttling
        if (onBestSolutionChangedEvent != null) {
            newSolverTask.addEventListener(bestSolutionChangedEvent -> {
                Future<?> bestSolutionChangedEventFuture = eventHandlerExecutorService.submit(
                        () -> onBestSolutionChangedEvent.accept(bestSolutionChangedEvent.getNewBestSolution()));
                problemIdToFuturesMap.get(problemId).add(bestSolutionChangedEventFuture);
            });
        }

        CompletableFuture<Solution_> solverFuture = CompletableFuture.supplyAsync(newSolverTask::startSolving, solverExecutorService);
        // TODO refactor lambdas in a separate method
        solverFuture.handle((solution_, throwable) -> {
            handleThrowable(throwable, onException, "Exception while solving problem ({" + problemId + "}).");
            if (onSolvingEnded != null) {
                CompletableFuture<Void> solvingEndedFuture = CompletableFuture.supplyAsync(() -> {
                    onSolvingEnded.accept(solution_);
                    return null;
                }, eventHandlerExecutorService);
                // possible NPE if this is invoked after cleanupResources, unless solverFuture is cancelled first
                problemIdToFuturesMap.get(problemId).add(solvingEndedFuture);
                return solvingEndedFuture;
            }
            return null;
        }).handle((voidCompletableFuture, throwable) -> { // clean-up resources after onSolvingEnded returns
            handleThrowable(throwable, onException, "Exception while executing onSolvingEnded of problem ({" + problemId + "}).");
            return cleanupResources(problemId);
        });
        problemIdToFuturesMap.get(problemId).add(solverFuture);
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
    public void terminateSolver(Object problemId) {
        Objects.requireNonNull(problemId);
        logger.info("Terminating solver of problemId ({}).", problemId);
        synchronized (this) { // should be synchronized to avoid cleaning up resources in onSolvingEnded if it's executed after solverTask.terminateSolver() and before cleanUpResources()
            if (!isProblemSubmitted(problemId)) {
                throw new IllegalArgumentException("Problem (" + problemId + ") either was not submitted or finished solving.");
            }
            problemIdToSolverTaskMap.get(problemId).terminateEarly();

            // TODO consider not cancelling futures and not cleaning up resources manually for graceful termination
            //     con: need a feedback mechanism to inform the user after termination is done
            //   Problem if not cancelled: submit a problem -> terminate -> re-submit same problem
            //     -> onSolvingEnded for first submission is invoked and might override recent data updates by solving again
            List<Future> problemEventHandlersFutures = problemIdToFuturesMap.get(problemId);
            problemEventHandlersFutures.forEach(future -> future.cancel(false)); // if the event handler is executing (i.e. in the middle of persisting) don't interrupt it
            cleanupResources(problemId);
        }
    }

    @Override
    public boolean isProblemSubmitted(Object problemId) {
        Objects.requireNonNull(problemId);
        return problemIdToSolverTaskMap.containsKey(problemId);
    }

    @Override
    public Map<Object, Indictment> getIndictmentMap(Solution_ solution) {
        ScoreDirector<Solution_> scoreDirector = solverFactory.buildSolver().getScoreDirectorFactory().buildScoreDirector(); // TODO compare against creating an indictmentMapSolver field
        scoreDirector.setWorkingSolution(solution);
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
        problemIdToSolverTaskMap.keySet().forEach(this::cleanupResources);
    }

    private synchronized boolean cleanupResources(Object problemId) {
        if (isProblemSubmitted(problemId)) {
            logger.info("Cleaning up resources for problemId ({}).", problemId);
            problemIdToSolverTaskMap.remove(problemId);
            problemIdToFuturesMap.remove(problemId);
            return true;
        }
        return false;
    }
}
