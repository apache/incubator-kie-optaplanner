package org.drools.planner.examples.travelingtournament.app.smart;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentBenchmarkApp extends CommonBenchmarkApp {

    public static final String SOLVER_BENCHMARK_CONFIG_PREFIX
            = "/org/drools/planner/examples/travelingtournament/benchmark/smart/";
    public static final String SOLVER_BENCHMARK_CONFIG
            = SOLVER_BENCHMARK_CONFIG_PREFIX + "smartTravelingTournamentSolverBenchmarkConfig.xml";

    public static void main(String[] args) {
        String solverBenchmarkConfig;
        // default is a workaround for http://jira.codehaus.org/browse/MEXEC-35
        if (args.length > 0 && !args[0].equals("default")) {
            solverBenchmarkConfig = SOLVER_BENCHMARK_CONFIG_PREFIX + args[0] + "SolverBenchmarkConfig.xml";
        } else {
            solverBenchmarkConfig = SOLVER_BENCHMARK_CONFIG;
        }
        new SmartTravelingTournamentBenchmarkApp(solverBenchmarkConfig).process();
    }

    public SmartTravelingTournamentBenchmarkApp(String solverBenchmarkConfig) {
        super(solverBenchmarkConfig, TravelingTournament.class);
    }

}
