package org.drools.solver.examples.manners2009.app;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.itc2007.examination.swingui.ExaminationPanel;
import org.drools.solver.examples.itc2007.examination.app.ExaminationApp;
import org.drools.solver.examples.manners2009.swingui.Manners2009Panel;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009App extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/manners2009/solver/manners2009SolverConfig.xml";

    public static void main(String[] args) {
        new Manners2009App().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected String getExampleDirName() {
        return "manners2009";
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new Manners2009Panel();
    }

}