package org.drools.planner.examples.pas.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.manners2009.persistence.Manners2009DaoImpl;
import org.drools.planner.examples.pas.persistence.PatientAdmissionScheduleDaoImpl;
import org.drools.planner.examples.pas.persistence.PatientAdmissionScheduleSolutionExporter;
import org.drools.planner.examples.pas.persistence.PatientAdmissionScheduleSolutionImporter;
import org.drools.planner.examples.pas.swingui.PatientAdmissionSchedulePanel;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/pas/solver/patientAdmissionScheduleSolverConfig.xml";

    public static void main(String[] args) {
        new PatientAdmissionScheduleApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new PatientAdmissionSchedulePanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new PatientAdmissionScheduleDaoImpl();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new PatientAdmissionScheduleSolutionImporter();
    }

    @Override
    protected AbstractSolutionExporter createSolutionExporter() {
        return new PatientAdmissionScheduleSolutionExporter();
    }

}
