package org.drools.planner.examples.curriculumcourse.app;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.curriculumcourse.domain.CurriculumCourseSchedule;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/curriculumcourse/benchmark/curriculumCourseSolverBenchmarkConfig.xml";
    public static final String SHORT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/curriculumcourse/benchmark/curriculumCourseShortSolverBenchmarkConfig.xml";
    public static final File SOLVER_BENCHMARK_RESULT_FILE
            = new File("local/data/curriculumcourse/curriculumCourseSolverBenchmarkResult.xml");

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0 && args[0].equals("short")) {
            solverConfig = SHORT_SOLVER_BENCHMARK_CONFIG;
        } else {
            solverConfig = DEFAULT_SOLVER_BENCHMARK_CONFIG;
        }
        new CurriculumCourseBenchmarkApp(solverConfig, SOLVER_BENCHMARK_RESULT_FILE).process();
    }

    public CurriculumCourseBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(solverBenchmarkConfig, resultFile, CurriculumCourseSchedule.class);
    }

}