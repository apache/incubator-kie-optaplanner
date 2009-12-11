package org.drools.planner.examples.nqueens;

import junit.framework.TestCase;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.common.persistence.SolutionDao;

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
        SolutionDao solutionDao = new XstreamSolutionDaoImpl();
        Solution startingSolution = solutionDao.readSolution(getClass().getResourceAsStream(UNSOLVED_DATA));
        solver.setStartingSolution(startingSolution);
        solver.solve();
        Solution bestSolution = solver.getBestSolution();
        assertNotNull(bestSolution);
        Score bestScore = solver.getBestSolution().getScore();
        assertEquals(DefaultSimpleScore.valueOf(0), bestScore);
    }

}
