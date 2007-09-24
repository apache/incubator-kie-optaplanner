package org.drools.solver.examples.travelingtournament.app.smart;

import java.io.File;

import org.drools.solver.examples.common.app.CommonBenchmarkApp;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentBenchmarkApp extends CommonBenchmarkApp {

    public static final String SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/travelingtournament/benchmark/smart/smartTravelingTournamentSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/travelingtournament/smart/smartTravelingTournamentSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        new SmartTravelingTournamentBenchmarkApp().process();
    }

    @Override
    protected String getSolverBenchmarkConfig() {
        return SOLVER_BENCHMARK_CONFIG;
    }

    @Override
    protected File getResultFile() {
        return SOLVER_BENCHMARK_RESULT_FILE;
    }

}
