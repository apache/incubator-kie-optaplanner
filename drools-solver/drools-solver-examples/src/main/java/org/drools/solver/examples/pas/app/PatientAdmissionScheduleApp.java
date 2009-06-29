package org.drools.solver.examples.pas.app;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.pas.swingui.PatientAdmissionSchedulePanel;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/pas/solver/patientAdmissionScheduleSolverConfig.xml";

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
    protected String getExampleDirName() {
        return "patientadmissionschedule";
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new PatientAdmissionSchedulePanel();
    }

}