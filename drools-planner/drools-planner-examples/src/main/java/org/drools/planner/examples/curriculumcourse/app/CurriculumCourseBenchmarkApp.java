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
    public static final String STEP_LIMIT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/curriculumcourse/benchmark/curriculumCourseStepLimitSolverBenchmarkConfig.xml";

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
        new CurriculumCourseBenchmarkApp(solverConfig).process();
    }

    public CurriculumCourseBenchmarkApp(String solverBenchmarkConfig) {
        super(solverBenchmarkConfig, CurriculumCourseSchedule.class);
    }

}
