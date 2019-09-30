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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaplanner.core.api.solver.manager.SolverManager;
import org.optaplanner.core.api.solver.manager.SolverStatus;
import org.optaplanner.core.config.solver.testutil.MockThreadFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultSolverManagerTest {

    public static final String SOLVER_CONFIG = "org/optaplanner/core/impl/solver/testdataSolverConfigXStream.xml";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SolverManager<TestdataSolution> solverManager;
    private Long tenantId;
    private CountDownLatch solutionChangedLatch;
    private CountDownLatch solvingEndedLatch;

    @Before
    public void setup() {
        solverManager = SolverManager.createFromXmlResource(SOLVER_CONFIG);
        tenantId = 0L;
        solutionChangedLatch = new CountDownLatch(1);
        solvingEndedLatch = new CountDownLatch(1);
    }

    @After
    public void tearDown() {
        solverManager.shutdown();
    }

    @Test(timeout = 5000L)
    public void basicUsageOfSolverManagerWithOneProblem() throws InterruptedException {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem,
                            taskAssigningSolution -> solutionChangedLatch.countDown(),
                            taskAssigningSolution -> solvingEndedLatch.countDown());
        solutionChangedLatch.await(5, TimeUnit.SECONDS);

        assertEquals(SolverStatus.SOLVING, solverManager.getSolverStatus(tenantId));
        assertEquals(tenantId.toString(), solverManager.getBestSolution(tenantId).getCode());
        assertTrue(solverManager.getBestScore(tenantId).isSolutionInitialized());

        solverManager.stopSolver(tenantId);
        assertEquals(SolverStatus.TERMINATED_EARLY, solverManager.getSolverStatus(tenantId));
        logger.info(String.valueOf(solvingEndedLatch.getCount()));
    }

    @Test(timeout = 5000L)
    public void customThreadFactoryClassIsUsed() {
        solverManager = SolverManager.createFromXmlResource(SOLVER_CONFIG, new MockThreadFactory());
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem, taskAssigningSolution -> solutionChangedLatch.countDown(), null);
        solutionChangedLatch.countDown();
        assertTrue(MockThreadFactory.hasBeenCalled());
    }

    @Test(timeout = 5000L)
    public void createSolverManagerWithNullArgument() {
        assertThatThrownBy(() -> SolverManager.createFromXmlResource(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SolverManager.createFromXmlResource(SOLVER_CONFIG, (ClassLoader) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SolverManager.createFromXmlResource(SOLVER_CONFIG, (ThreadFactory) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SolverManager.createFromXmlResource(SOLVER_CONFIG, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test(timeout = 5000L)
    public void onBestSolutionChangeAndOnSolutionEnded() throws InterruptedException {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem,
                            taskAssigningSolution -> solutionChangedLatch.countDown(),
                            taskAssigningSolution -> solvingEndedLatch.countDown());
        solutionChangedLatch.await(5, TimeUnit.SECONDS);
        assertEquals(SolverStatus.SOLVING, solverManager.getSolverStatus(tenantId));
        solverManager.stopSolver(tenantId);
        solvingEndedLatch.await(5, TimeUnit.SECONDS);
        assertEquals(SolverStatus.TERMINATED_EARLY, solverManager.getSolverStatus(tenantId));
    }

    @Test(timeout = 5000L)
    public void tryToGetNonExistingSolution() throws InterruptedException {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem,
                            taskAssigningSolution -> solutionChangedLatch.countDown(),
                            taskAssigningSolution -> solvingEndedLatch.countDown());
        solutionChangedLatch.await(60, TimeUnit.SECONDS);
        assertNull(solverManager.getBestSolution(tenantId + 1));
    }

    @Test(timeout = 5000L)
    public void onBestSolutionChangedCalledWhenASolutionIsChanged() throws InterruptedException {
        AtomicInteger bestSolutionChangedEventCount = new AtomicInteger(0);
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem,
                            taskAssigningSolution -> bestSolutionChangedEventCount.incrementAndGet(),
                            taskAssigningSolution -> solvingEndedLatch.countDown());

        // TODO remove sleep and stop solver, instead configure SolverManager to adjust solving duration
        Thread.sleep(500L);
        solverManager.stopSolver(tenantId);

        solvingEndedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(bestSolutionChangedEventCount.get() > 0);
        logger.info("Number of bestSolutionChangedEvents: {}.", bestSolutionChangedEventCount.get());
    }

    @Test
    public void shutdownShouldStopAllSolvers() {
        int problemCount = Runtime.getRuntime().availableProcessors() * 3;
        IntStream.range(0, problemCount)
                .forEach(problemId -> {
                    TestdataSolution problem = createTestProblem((long) problemId);
                    solverManager.solve(problemId, problem, null, null);
                });
        solverManager.shutdown(); // Calling it right away while some tasks might be on the queue
        IntStream.range(0, problemCount)
                .forEach(problemId -> assertEquals(SolverStatus.TERMINATED_EARLY, solverManager.getSolverStatus(problemId)));
    }

    // ****************************
    // Exception handling tests
    // ****************************

    @Test(timeout = 5000L)
    public void shouldNotStartTwoSolverTasksWithSameProblemId() {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem, null, null);
        assertThatThrownBy(() -> solverManager.solve(tenantId, problem, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Problem (" + tenantId + ") already exists.");
    }

    @Test
    public void shouldPropagateExceptionsFromSolverThread() throws Throwable {
        TestdataSolution problem = createTestProblem(tenantId);
        problem.setValueList(null); // So that solver thread throws IllegalArgumentException
        final AtomicReference<Throwable> solverException = new AtomicReference<>();
        solverManager.solve(tenantId, problem, null,
                            solution -> solvingEndedLatch.countDown(),
                            solverException::set);
        solvingEndedLatch.await(10, TimeUnit.SECONDS);
        assertEquals(IllegalArgumentException.class, solverException.get().getClass());
    }

    @Test(timeout = 5000L)
    public void shouldNotStopASolverThatHasNotBeenSubmitted() {
        assertThatThrownBy(() -> solverManager.stopSolver(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Problem (" + tenantId + ") was not submitted.");
    }

    @Test(timeout = 5000L)
    public void shouldNotGetMetadataOfSolverThatHasNotBeenSubmitted() {
        assertNull(solverManager.getBestSolution(tenantId));
        assertNull(solverManager.getBestScore(tenantId));
        assertNull(solverManager.getSolverStatus(tenantId));
    }

    private TestdataSolution createTestProblem(Long tenantId) {
        TestdataSolution solution = new TestdataSolution("s1");
        solution.setCode(tenantId.toString());
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        solution.setEntityList(Arrays.asList(new TestdataEntity("e1"), new TestdataEntity("e2")));
        return solution;
    }
}
