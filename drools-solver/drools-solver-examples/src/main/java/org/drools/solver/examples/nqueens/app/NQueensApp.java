package org.drools.solver.examples.nqueens.app;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.nqueens.swingui.NQueensPanel;

/**
 * @author Geoffrey De Smet
 */
public class NQueensApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/nqueens/solver/nqueensSolverConfig.xml";

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
    protected String getExampleDirName() {
        return "nqueens";
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new NQueensPanel();
    }

}
