/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.common.app;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.common.TestSystemProperties;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Runs an example {@link Solver}.
 * <p>
 * A test should run in less than 10 seconds on a 3 year old desktop computer, choose the bestScoreLimit accordingly.
 * Always use a {@link Timeout} on {@link Test}, preferably 10 minutes because some of the Jenkins machines are old.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class SolverPerformanceTest<Solution_> extends LoggingTest {

    private static final String MOVE_THREAD_COUNTS_STRING = System.getProperty(TestSystemProperties.MOVE_THREAD_COUNTS);

    protected SolutionFileIO<Solution_> solutionFileIO;
    protected String solverConfigResource;

    static Stream<String> moveThreadCounts() {
        return Optional.ofNullable(MOVE_THREAD_COUNTS_STRING)
                .map(s -> Arrays.stream(s.split(",")))
                .orElse(Stream.of(SolverConfig.MOVE_THREAD_COUNT_NONE));
    }

    @TestFactory
    @Timeout(600)
    Stream<DynamicTest> testSpeed() {
        return moveThreadCounts().flatMap(moveThreadCount -> testData().map(testData ->
                dynamicTest(
                        testData.unsolvedDataFile.replaceFirst(".*/", "")
                                + ", "
                                + testData.environmentMode
                                + ", threads: " + moveThreadCount,
                        () -> runSpeedTest(
                                new File(testData.unsolvedDataFile),
                                testData.bestScoreLimit,
                                testData.environmentMode,
                                moveThreadCount)
                )));
    }

    @BeforeEach
    public void setUp() {
        CommonApp<Solution_> commonApp = createCommonApp();
        solutionFileIO = commonApp.createSolutionFileIO();
        solverConfigResource = commonApp.getSolverConfigResource();
    }

    protected abstract CommonApp<Solution_> createCommonApp();

    protected abstract Stream<TestData> testData();

    private void runSpeedTest(
            File unsolvedDataFile,
            String bestScoreLimitString,
            EnvironmentMode environmentMode,
            String moveThreadCount) {
        SolverFactory<Solution_> solverFactory = buildSolverFactory(
                bestScoreLimitString, environmentMode, moveThreadCount);
        Solution_ problem = solutionFileIO.read(unsolvedDataFile);
        logger.info("Opened: {}", unsolvedDataFile);
        Solver<Solution_> solver = solverFactory.buildSolver();
        Solution_ bestSolution = solver.solve(problem);
        assertScoreAndConstraintMatches(solverFactory, bestSolution, bestScoreLimitString);
    }

    protected SolverFactory<Solution_> buildSolverFactory(
            String bestScoreLimitString,
            EnvironmentMode environmentMode,
            String moveThreadCount) {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(solverConfigResource);
        solverConfig.withEnvironmentMode(environmentMode)
                .withTerminationConfig(new TerminationConfig()
                        .withBestScoreLimit(bestScoreLimitString))
                .withMoveThreadCount(moveThreadCount);
        return SolverFactory.create(solverConfig);
    }

    private void assertScoreAndConstraintMatches(SolverFactory<Solution_> solverFactory,
            Solution_ bestSolution, String bestScoreLimitString) {
        assertNotNull(bestSolution);
        InnerScoreDirectorFactory<Solution_> scoreDirectorFactory
                = (InnerScoreDirectorFactory<Solution_>) solverFactory.getScoreDirectorFactory();
        Score bestScore = scoreDirectorFactory.getSolutionDescriptor().getScore(bestSolution);
        ScoreDefinition scoreDefinition = scoreDirectorFactory.getScoreDefinition();
        Score bestScoreLimit = scoreDefinition.parseScore(bestScoreLimitString);
        assertTrue("The bestScore (" + bestScore + ") must be at least the bestScoreLimit (" + bestScoreLimit + ").",
                bestScore.compareTo(bestScoreLimit) >= 0);

        try (ScoreDirector<Solution_> scoreDirector = scoreDirectorFactory.buildScoreDirector()) {
            scoreDirector.setWorkingSolution(bestSolution);
            Score score = scoreDirector.calculateScore();
            assertEquals(score, bestScore);
            if (scoreDirector.isConstraintMatchEnabled()) {
                Collection<ConstraintMatchTotal> constraintMatchTotals = scoreDirector.getConstraintMatchTotals();
                assertNotNull(constraintMatchTotals);
                assertEquals(score, constraintMatchTotals.stream()
                        .map(ConstraintMatchTotal::getScore)
                        .reduce(Score::add)
                        .orElse(scoreDefinition.getZeroScore()));
                assertNotNull(scoreDirector.getIndictmentMap());
            }
        }
    }

    protected static TestData testData(String unsolvedDataFile, String bestScoreLimit, EnvironmentMode environmentMode) {
        return new TestData(unsolvedDataFile, bestScoreLimit, environmentMode);
    }

    protected static class TestData {

        final String unsolvedDataFile;
        final String bestScoreLimit;
        final EnvironmentMode environmentMode;

        public TestData(String unsolvedDataFile, String bestScoreLimit, EnvironmentMode environmentMode) {
            this.unsolvedDataFile = unsolvedDataFile;
            this.bestScoreLimit = bestScoreLimit;
            this.environmentMode = environmentMode;
        }
    }
}
