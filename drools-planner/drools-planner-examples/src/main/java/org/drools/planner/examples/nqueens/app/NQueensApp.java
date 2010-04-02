package org.drools.planner.examples.nqueens.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.nqueens.persistence.NQueensDaoImpl;
import org.drools.planner.examples.nqueens.swingui.NQueensPanel;

/**
 * @author Geoffrey De Smet
 */
public class NQueensApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/nqueens/solver/nqueensSolverConfig.xml";

    public static void main(String[] args) {
        new NQueensApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new NQueensPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new NQueensDaoImpl();
    }

}
