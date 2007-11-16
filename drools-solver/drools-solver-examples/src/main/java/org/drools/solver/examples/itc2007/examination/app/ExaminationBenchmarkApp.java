package org.drools.solver.examples.itc2007.examination.app;

import java.io.File;

import org.drools.solver.examples.common.app.CommonBenchmarkApp;
import org.drools.solver.examples.nqueens.app.NQueensBenchmarkApp;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationBenchmarkApp extends CommonBenchmarkApp {

    public static final String SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/itc2007/examination/benchmark/examinationSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/itc2007/examination/examinationSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        new NQueensBenchmarkApp(SOLVER_BENCHMARK_CONFIG, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public ExaminationBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile);
    }

}