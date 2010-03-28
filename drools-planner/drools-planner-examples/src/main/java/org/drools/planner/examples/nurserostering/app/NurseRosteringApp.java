package org.drools.planner.examples.nurserostering.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringDaoImpl;
import org.drools.planner.examples.nurserostering.swingui.NurseRosteringPanel;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/nurserostering/solver/nurseRosteringSolverConfig.xml";

    public static void main(String[] args) {
        new NurseRosteringApp().init();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new NurseRosteringDaoImpl();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new NurseRosteringPanel();
    }

}
