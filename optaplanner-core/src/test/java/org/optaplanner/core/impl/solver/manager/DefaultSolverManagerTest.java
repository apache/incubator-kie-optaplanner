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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.manager.SolverManager;
import org.optaplanner.core.config.solver.testutil.MockThreadFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// TODO test submitting two different problems with two instances of problemId where problemId1.equals(problemId2) is true
public class DefaultSolverManagerTest {

    public static final String SOLVER_CONFIG = "org/optaplanner/core/api/solver/testdataSolverConfig.xml";
    private static final long TIMEOUT_5000L = 5000L;

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

    @Test(timeout = TIMEOUT_5000L)
    public void basicUsageOfSolverManagerWithOneProblem() throws InterruptedException {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem,
                            taskAssigningSolution -> solutionChangedLatch.countDown(),
                            taskAssigningSolution -> solvingEndedLatch.countDown());
        solutionChangedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(solverManager.isProblemSubmitted(tenantId));
        solverManager.terminateSolver(tenantId);
        solvingEndedLatch.await(5, TimeUnit.SECONDS);
    }

    @Test(timeout = TIMEOUT_5000L)
    public void customThreadFactoryClassIsUsed() throws InterruptedException {
        solverManager = SolverManager.createFromXmlResource(SOLVER_CONFIG, new MockThreadFactory());
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem, taskAssigningSolution -> solutionChangedLatch.countDown(), null);
        solutionChangedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(MockThreadFactory.hasBeenCalled());
    }

    @Test(timeout = TIMEOUT_5000L)
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

    @Test(timeout = TIMEOUT_5000L)
    public void createSolverManagerFromSolverFactory() throws InterruptedException {
        solverManager = SolverManager.createFromSolverFactory(SolverFactory.createFromXmlResource(SOLVER_CONFIG));
        basicUsageOfSolverManagerWithOneProblem();
    }

    @Test(timeout = TIMEOUT_5000L)
    public void customThreadFactoryClassIsUsedWithSolverFactory() throws InterruptedException {
        solverManager = SolverManager.createFromSolverFactory(SolverFactory.createFromXmlResource(SOLVER_CONFIG), new MockThreadFactory());
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solveBestEvents(tenantId, problem, taskAssigningSolution -> solutionChangedLatch.countDown());
        solutionChangedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(MockThreadFactory.hasBeenCalled());
    }

    @Test(timeout = TIMEOUT_5000L)
    public void generatedProblemId() {
        TestdataSolution problem = createTestProblem(tenantId);
        UUID problemId = solverManager.solveBatch(problem, taskAssigningSolution -> solutionChangedLatch.countDown());
        assertTrue(solverManager.isProblemSubmitted(problemId));
    }

    @Test(timeout = TIMEOUT_5000L)
    public void cannotSolveBatchWithoutEventHandler() {
        TestdataSolution problem = createTestProblem(tenantId);
        assertThatNullPointerException()
                .isThrownBy(() -> solverManager.solveBatch(tenantId, problem, null));
    }

    @Test(timeout = TIMEOUT_5000L)
    public void cannotSolveBestEventsWithoutEventHandler() {
        TestdataSolution problem = createTestProblem(tenantId);
        assertThatNullPointerException()
                .isThrownBy(() -> solverManager.solveBestEvents(tenantId, problem, null));
    }

    @Test(timeout = TIMEOUT_5000L)
    public void shutdownShouldStopAllSolvers() throws InterruptedException {
        int problemCount = Runtime.getRuntime().availableProcessors() * 3;
        IntStream.range(0, problemCount)
                .forEach(problemId -> {
                    TestdataSolution problem = createTestProblem((long) problemId);
                    solverManager.solve(problemId, problem, null, null);
                });
        solverManager.shutdown(); // Calling it right away while some tasks might be on the queue
        IntStream.range(0, problemCount)
                .forEach(problemId -> {
                    assertFalse(solverManager.isProblemSubmitted(problemId));
                });
    }

    // ****************************
    // Exception handling tests
    // ****************************

    @Test(timeout = TIMEOUT_5000L)
    public void shouldNotStartTwoSolverTasksWithSameProblemId() {
        TestdataSolution problem = createTestProblem(tenantId);
        solverManager.solve(tenantId, problem, null, null);
        assertThatThrownBy(() -> solverManager.solve(tenantId, problem, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Problem (" + tenantId + ") already exists.");
    }

    @Test(timeout = TIMEOUT_5000L)
    public void shouldPropagateExceptionsFromSolverThread() throws Throwable {
        TestdataSolution problem = createTestProblem(tenantId);
        problem.setValueList(null); // So that solver thread throws IllegalArgumentException
        final AtomicReference<Throwable> solverException = new AtomicReference<>();
        solverManager.solveBatch(tenantId, problem, solution -> solvingEndedLatch.countDown(), solverException::set);
        solvingEndedLatch.await(10, TimeUnit.SECONDS);
        assertEquals(IllegalArgumentException.class, solverException.get().getClass());
    }

    @Test(timeout = TIMEOUT_5000L)
    public void shouldNotStopASolverThatHasNotBeenSubmitted() {
        assertThatThrownBy(() -> solverManager.terminateSolver(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Problem (" + tenantId + ") either was not submitted or finished solving.");
    }

    private TestdataSolution createTestProblem(Long tenantId) {
        TestdataSolution solution = new TestdataSolution("s1");
        solution.setCode(tenantId.toString());
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        solution.setEntityList(Arrays.asList(new TestdataEntity("e1"), new TestdataEntity("e2")));
        return solution;
    }
}
