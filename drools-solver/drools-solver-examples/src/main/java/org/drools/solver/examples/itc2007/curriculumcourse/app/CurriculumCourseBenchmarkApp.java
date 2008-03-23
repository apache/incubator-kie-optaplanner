package org.drools.solver.examples.itc2007.curriculumcourse.app;

import java.io.File;

import org.drools.solver.examples.common.app.CommonBenchmarkApp;
import org.drools.solver.examples.nqueens.app.NQueensBenchmarkApp;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/itc2007/curriculumcourse/benchmark/curriculumCourseSolverBenchmarkConfig.xml";
    public static final String SHORT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/solver/examples/itc2007/curriculumcourse/benchmark/curriculumCourseShortSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/itc2007/curriculumcourse/curriculumCourseSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0 && args[0].equals("short")) {
            solverConfig = SHORT_SOLVER_BENCHMARK_CONFIG;
        } else {
            solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
        }
        new NQueensBenchmarkApp(solverConfig, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public CurriculumCourseBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile);
    }

}