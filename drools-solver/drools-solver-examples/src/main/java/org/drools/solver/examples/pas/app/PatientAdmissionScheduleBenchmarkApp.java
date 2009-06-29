package org.drools.solver.examples.pas.app;

import java.io.File;

import org.drools.solver.examples.common.app.CommonBenchmarkApp;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/pas/benchmark/patientAdmissionScheduleSolverBenchmarkConfig.xml";
    public static final String SHORT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/pas/benchmark/patientAdmissionScheduleShortSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/pas/patientAdmissionScheduleSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0 && args[0].equals("short")) {
            solverConfig = SHORT_SOLVER_BENCHMARK_CONFIG;
        } else {
            solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
        }
        new PatientAdmissionScheduleBenchmarkApp(solverConfig, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public PatientAdmissionScheduleBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile);
    }

}