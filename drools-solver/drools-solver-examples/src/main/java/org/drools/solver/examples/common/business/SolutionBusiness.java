package org.drools.solver.examples.common.business;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import org.drools.solver.core.Solver;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.persistence.SolutionDao;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author Geoffrey De Smet
 */
public class SolutionBusiness {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolutionDao solutionDao;

    private File unsolvedDataDir;
    private File solvedDataDir;

    private Solver solver;
    private EvaluationHandler evaluationHandler;

    public void setSolutionDao(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
    }

    public void setDataDir(File dataDir) {
        unsolvedDataDir = new File(dataDir, "unsolved");
        solvedDataDir = new File(dataDir, "solved");
    }

    public File getUnsolvedDataDir() {
        return unsolvedDataDir;
    }

    public File getSolvedDataDir() {
        return solvedDataDir;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
        this.evaluationHandler = solver.getEvaluationHandler();
    }


    public List<File> getUnsolvedFileList() {
        return Arrays.asList(unsolvedDataDir.listFiles(new SolverExampleFileFilter()));
    }

    public List<File> getSolvedFileList() {
        return Arrays.asList(solvedDataDir.listFiles(new SolverExampleFileFilter()));
    }

    public Solution getSolution() {
        return evaluationHandler.getSolution();
    }

    public double getScore() {
        return evaluationHandler.fireAllRulesAndCalculateStepScore();
    }

    public void load(File file) {
        Solution solution = solutionDao.readSolution(file);
        evaluationHandler.setSolution(solution);
    }

    public void save(File file) {
        Solution solution = evaluationHandler.getSolution();
        solutionDao.writeSolution(solution, file);
    }

    public void doMove(Move move) {
        logger.info("Doing user move ({})", move);
        move.doMove(evaluationHandler.getStatefulSession());
    }

    public void solve() {
        solver.solve();
    }

    public class SolverExampleFileFilter implements FileFilter {

        public boolean accept(File file) {
            if (file.isDirectory() || file.isHidden()) {
                return false;
            }
            return file.getName().endsWith(".xml");
        }

    }
    
}
