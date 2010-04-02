package org.drools.planner.examples.nurserostering.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringDaoImpl;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringSolutionExporter;
import org.drools.planner.examples.nurserostering.persistence.NurseRosteringSolutionImporter;
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
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new NurseRosteringPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new NurseRosteringDaoImpl();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new NurseRosteringSolutionImporter();
    }

    @Override
    protected AbstractSolutionExporter createSolutionExporter() {
        return new NurseRosteringSolutionExporter();
    }

}
