package org.drools.planner.examples.common.app;

import java.io.File;

import org.drools.planner.benchmark.XmlSolverBenchmarker;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonBenchmarkApp extends LoggingMain {

    private static final String LOGGING_CONFIG = "/org/drools/planner/examples/common/app/log4j-benchmark.xml";

    protected XmlSolverBenchmarker solverBenchmarker;

    protected CommonBenchmarkApp(String solverBenchmarkConfig, Class ... xstreamAnnotations) {
        super(LOGGING_CONFIG);
        solverBenchmarker = new XmlSolverBenchmarker().configure(solverBenchmarkConfig);
        solverBenchmarker.addXstreamAnnotations(xstreamAnnotations);
    }

    public void process() {
        solverBenchmarker.benchmark();
    }

}
