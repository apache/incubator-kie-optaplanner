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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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

/**
 * Runs an example {@link Solver}.
 * <p>
 * A test should run in less than 10 seconds on a 3 year old desktop computer, choose the bestScoreLimit accordingly.
 * Always use a {@link Test#timeout()} on {@link Test}, preferably 10 minutes because some of the Jenkins machines are old.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@RunWith(Parameterized.class)
public abstract class SolverPerformanceTest<Solution_> extends LoggingTest {

    private static final String MOVE_THREAD_COUNTS_STRING = System.getProperty(TestSystemProperties.MOVE_THREAD_COUNTS);

    private final String moveThreadCount;

    protected SolutionFileIO<Solution_> solutionFileIO;
    protected String solverConfig;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> getSolutionFilesAsParameters() {
        List<Object[]> testParams = new ArrayList<>();

        if (MOVE_THREAD_COUNTS_STRING != null) {
            String[] moveThreadCounts = MOVE_THREAD_COUNTS_STRING.split(",");
            for (String moveThreadCount : moveThreadCounts) {
                testParams.add(new Object[]{moveThreadCount});
            }
        } else {
            testParams.add(new Object[]{SolverConfig.MOVE_THREAD_COUNT_NONE});
        }
        return testParams;
    }

    public SolverPerformanceTest(String moveThreadCount) {
        this.moveThreadCount = moveThreadCount;
    }

    @Before
    public void setUp() {
        CommonApp<Solution_> commonApp = createCommonApp();
        solutionFileIO = commonApp.createSolutionFileIO();
        solverConfig = commonApp.getSolverConfig();
    }

    protected abstract CommonApp<Solution_> createCommonApp();

    protected void runSpeedTest(File unsolvedDataFile, String bestScoreLimitString) {
        runSpeedTest(unsolvedDataFile, bestScoreLimitString, EnvironmentMode.REPRODUCIBLE);
    }

    protected void runSpeedTest(File unsolvedDataFile, String bestScoreLimitString, EnvironmentMode environmentMode) {
        SolverFactory<Solution_> solverFactory = buildSolverFactory(bestScoreLimitString, environmentMode);
        Solution_ problem = solutionFileIO.read(unsolvedDataFile);
        logger.info("Opened: {}", unsolvedDataFile);
        Solver<Solution_> solver = solverFactory.buildSolver();
        Solution_ bestSolution = solver.solve(problem);
        assertScoreAndConstraintMatches(solver, bestSolution, bestScoreLimitString);
    }

    protected SolverFactory<Solution_> buildSolverFactory(String bestScoreLimitString, EnvironmentMode environmentMode) {
        SolverFactory<Solution_> solverFactory = SolverFactory.createFromXmlResource(solverConfig);
        solverFactory.getSolverConfig().setEnvironmentMode(environmentMode);
        solverFactory.getSolverConfig().setTerminationConfig(
                new TerminationConfig().withBestScoreLimit(bestScoreLimitString));
        solverFactory.getSolverConfig().setMoveThreadCount(moveThreadCount);

        return solverFactory;
    }

    private void assertScoreAndConstraintMatches(Solver<Solution_> solver, Solution_ bestSolution, String bestScoreLimitString) {
        assertNotNull(bestSolution);
        InnerScoreDirectorFactory<Solution_> scoreDirectorFactory
                = (InnerScoreDirectorFactory<Solution_>) solver.getScoreDirectorFactory();
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
                        .reduce(Score::add).orElse(scoreDefinition.getZeroScore()));
                assertNotNull(scoreDirector.getIndictmentMap());
            }
        }
    }

}
