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

import org.junit.Before;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.examples.common.persistence.SolutionDao;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs an example solver.
 * All tests ending with the suffix <code>PerformanceTest</code> are reported on by hudson
 * in graphs which show the execution time over builds.
 * <p>
 * Recommended courtesy notes: Always use a timeout value on @Test.
 * The timeout should be the triple of the timeout on a normal 3 year old desktop computer,
 * because some of the hudson machines are old.
 * For example, on a normal 3 year old desktop computer it always finishes in less than 1 minute,
 * then specify a timeout of 3 minutes.
 */
public abstract class SolverPerformanceTest<Solution_> extends LoggingTest {

    protected SolutionDao<Solution_> solutionDao;

    @Before
    public void setUp() {
        solutionDao = createSolutionDao();
    }

    protected abstract String createSolverConfigResource();

    protected abstract SolutionDao<Solution_> createSolutionDao();

    protected void runSpeedTest(File unsolvedDataFile, String bestScoreLimitString) {
        runSpeedTest(unsolvedDataFile, bestScoreLimitString, EnvironmentMode.REPRODUCIBLE);
    }

    protected void runSpeedTest(File unsolvedDataFile, String bestScoreLimitString, EnvironmentMode environmentMode) {
        SolverFactory<Solution_> solverFactory = buildSolverFactory(bestScoreLimitString, environmentMode);
        Solution_ planningProblem = solutionDao.readSolution(unsolvedDataFile);
        Solver<Solution_> solver = solverFactory.buildSolver();
        Solution_ bestSolution = solver.solve(planningProblem);
        assertBestSolution(solver, bestSolution, bestScoreLimitString);
    }

    protected SolverFactory<Solution_> buildSolverFactory(String bestScoreLimitString, EnvironmentMode environmentMode) {
        SolverFactory<Solution_> solverFactory = SolverFactory.createFromXmlResource(createSolverConfigResource());
        solverFactory.getSolverConfig().setEnvironmentMode(environmentMode);
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setBestScoreLimit(bestScoreLimitString);
        solverFactory.getSolverConfig().setTerminationConfig(terminationConfig);
        return solverFactory;
    }

    private void assertBestSolution(Solver<Solution_> solver, Solution_ bestSolution, String bestScoreLimitString) {
        assertNotNull(bestSolution);
        InnerScoreDirectorFactory<Solution_> scoreDirectorFactory
                = (InnerScoreDirectorFactory<Solution_>) solver.getScoreDirectorFactory();
        Score bestScore = scoreDirectorFactory.getSolutionDescriptor().getScore(bestSolution);
        Score bestScoreLimit = scoreDirectorFactory.getScoreDefinition().parseScore(bestScoreLimitString);
        assertTrue("The bestScore (" + bestScore + ") must be at least bestScoreLimit (" + bestScoreLimit + ").",
                bestScore.compareTo(bestScoreLimit) >= 0);
    }

}
