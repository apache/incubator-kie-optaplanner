package org.drools.solver.examples.nqueens;

import junit.framework.TestCase;
import org.drools.solver.core.Solver;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.DefaultSimpleScore;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.persistence.SolutionDao;

/**
 * @author Geoffrey De Smet
 */
public class NQueensSmokeTest extends TestCase {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/nqueens/solver/nqueensSmokeSolverConfig.xml";
    public static final String UNSOLVED_DATA
            = "/org/drools/solver/examples/nqueens/data/unsolvedNQueensSmoke.xml";


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
