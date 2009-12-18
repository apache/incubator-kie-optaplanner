package org.drools.planner.examples.nqueens.app;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.nqueens.domain.NQueens;

/**
 * @author Geoffrey De Smet
 */
public class NQueensBenchmarkApp extends CommonBenchmarkApp {

    public static final String SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/nqueens/benchmark/nqueensSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/nqueens/nqueensSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        new NQueensBenchmarkApp(SOLVER_BENCHMARK_CONFIG, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public NQueensBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile, NQueens.class);
    }

}
