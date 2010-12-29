/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.examples.common.app;

import java.io.File;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.config.localsearch.EnvironmentMode;
import org.drools.planner.config.localsearch.termination.TerminationConfig;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.SolutionDao;

import static org.junit.Assert.*;

/**
 * Runs an example solver.
 * All tests ending with the suffix <code>PerformanceTest</code> are reported on by hudson
 * in graphs which show the execution time over builds.
 * @author Geoffrey De Smet
 */
public abstract class SolverPerformanceTest extends LoggingTest {

    protected abstract String createSolverConfigResource();

    protected abstract SolutionDao createSolutionDao();

    protected void runSpeedTest(File unsolvedDataFile, String scoreAttainedString) {
        XmlSolverConfigurer configurer = buildConfigurer(scoreAttainedString);
        Solver solver = solve(configurer, unsolvedDataFile);
        assertBestSolution(solver, scoreAttainedString);
    }

    private XmlSolverConfigurer buildConfigurer(String scoreAttainedString) {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(createSolverConfigResource());
        configurer.getConfig().setEnvironmentMode(EnvironmentMode.DEBUG);
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setScoreAttained(scoreAttainedString);
        configurer.getConfig().setTerminationConfig(terminationConfig);
        return configurer;
    }

    private Solver solve(XmlSolverConfigurer configurer, File unsolvedDataFile) {
        SolutionDao solutionDao = createSolutionDao();
        Solution startingSolution = solutionDao.readSolution(unsolvedDataFile);
        Solver solver = configurer.buildSolver();
        solver.setStartingSolution(startingSolution);
        solver.solve();
        return solver;
    }

    private void assertBestSolution(Solver solver, String scoreAttainedString) {
        Solution bestSolution = solver.getBestSolution();
        assertNotNull(bestSolution);
        Score bestScore = bestSolution.getScore();
        Score scoreAttained = solver.getScoreDefinition().parseScore(scoreAttainedString);
        assertTrue("The bestScore (" + bestScore + ") must be at least scoreAttained (" + scoreAttained + ").",
                bestScore.compareTo(scoreAttained) >= 0);
    }

}
