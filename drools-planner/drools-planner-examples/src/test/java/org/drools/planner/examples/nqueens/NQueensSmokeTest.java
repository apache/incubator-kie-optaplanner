/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.nqueens;

import junit.framework.TestCase;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.nqueens.persistence.NQueensDaoImpl;

/**
 * @author Geoffrey De Smet
 */
public class NQueensSmokeTest extends TestCase {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/nqueens/solver/nqueensSmokeSolverConfig.xml";
    public static final String UNSOLVED_DATA
            = "/org/drools/planner/examples/nqueens/data/unsolvedNQueensSmoke.xml";


    public void testSmoke() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        Solver solver = configurer.buildSolver();
        SolutionDao solutionDao = new NQueensDaoImpl();
        Solution startingSolution = solutionDao.readSolution(getClass().getResourceAsStream(UNSOLVED_DATA));
        solver.setStartingSolution(startingSolution);
        solver.solve();
        Solution bestSolution = solver.getBestSolution();
        assertNotNull(bestSolution);
        Score bestScore = solver.getBestSolution().getScore();
        assertEquals(DefaultSimpleScore.valueOf(0), bestScore);
    }

}
