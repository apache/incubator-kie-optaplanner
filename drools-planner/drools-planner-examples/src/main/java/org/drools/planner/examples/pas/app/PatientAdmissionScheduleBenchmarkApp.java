package org.drools.planner.examples.pas.app;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/pas/benchmark/patientAdmissionScheduleSolverBenchmarkConfig.xml";
    public static final String SHORT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/pas/benchmark/patientAdmissionScheduleShortSolverBenchmarkConfig.xml";

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0 && args[0].equals("short")) {
            solverConfig = SHORT_SOLVER_BENCHMARK_CONFIG;
        } else {
            solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
        }
        new PatientAdmissionScheduleBenchmarkApp(solverConfig).process();
    }

    public PatientAdmissionScheduleBenchmarkApp(String solverBenchmarkConfig) {
        super(solverBenchmarkConfig, PatientAdmissionSchedule.class);
    }

}
