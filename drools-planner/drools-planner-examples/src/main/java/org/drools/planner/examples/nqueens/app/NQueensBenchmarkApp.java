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

    public static void main(String[] args) {
        new NQueensBenchmarkApp(SOLVER_BENCHMARK_CONFIG).process();
    }

    public NQueensBenchmarkApp(String solverBenchmarkConfig) {
        super(solverBenchmarkConfig, NQueens.class);
    }

}
