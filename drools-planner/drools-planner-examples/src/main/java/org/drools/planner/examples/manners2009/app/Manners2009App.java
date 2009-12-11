package org.drools.planner.examples.manners2009.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.manners2009.swingui.Manners2009Panel;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009App extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/manners2009/solver/manners2009SolverConfig.xml";

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