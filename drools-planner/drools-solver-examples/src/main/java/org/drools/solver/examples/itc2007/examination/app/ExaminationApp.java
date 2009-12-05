package org.drools.solver.examples.itc2007.examination.app;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.itc2007.examination.swingui.ExaminationPanel;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/itc2007/examination/solver/examinationSolverConfig.xml";

    public static void main(String[] args) {
        new ExaminationApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected String getExampleDirName() {
        return "itc2007/examination";
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new ExaminationPanel();
    }

}