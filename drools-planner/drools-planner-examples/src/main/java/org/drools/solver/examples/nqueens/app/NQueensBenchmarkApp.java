package org.drools.solver.examples.nqueens.app;

import java.io.File;

import org.drools.solver.examples.common.app.CommonBenchmarkApp;

/**
 * @author Geoffrey De Smet
 */
public class NQueensBenchmarkApp extends CommonBenchmarkApp {

    public static final String SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/nqueens/benchmark/nqueensSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/nqueens/nqueensSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        new NQueensBenchmarkApp(SOLVER_BENCHMARK_CONFIG, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public NQueensBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile);
    }

}
