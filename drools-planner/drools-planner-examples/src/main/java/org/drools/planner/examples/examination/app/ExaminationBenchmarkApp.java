package org.drools.planner.examples.examination.app;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/examination/benchmark/examinationSolverBenchmarkConfig.xml";
    public static final String STEP_LIMIT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/examination/benchmark/examinationStepLimitSolverBenchmarkConfig.xml";
    
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/examination/examinationSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0) {
            if (args[0].equals("default")) {
                solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
            } else if (args[0].equals("stepLimit")) {
                solverConfig = STEP_LIMIT_SOLVER_BENCHMARK_CONFIG;
            } else {
                throw new IllegalArgumentException("The program argument (" + args[0] + ") is not supported.");
            }
        } else {
            solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
        }
        new ExaminationBenchmarkApp(solverConfig, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public ExaminationBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile, Examination.class);
    }

}
