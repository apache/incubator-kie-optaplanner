package org.drools.planner.examples.examination.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.examination.persistence.ExaminationDaoImpl;
import org.drools.planner.examples.examination.swingui.ExaminationPanel;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/examination/solver/examinationSolverConfig.xml";

    public static void main(String[] args) {
        new ExaminationApp().init();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new ExaminationDaoImpl();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new ExaminationPanel();
    }

}