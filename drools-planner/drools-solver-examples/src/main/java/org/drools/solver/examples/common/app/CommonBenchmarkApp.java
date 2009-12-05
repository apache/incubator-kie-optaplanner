package org.drools.solver.examples.common.app;

import java.io.File;

import org.drools.solver.benchmark.XmlSolverBenchmarker;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonBenchmarkApp extends LoggingMain {

    private static final String LOGGING_CONFIG = "/org/drools/solver/examples/common/app/log4j-benchmark.xml";

    private File resultFile;
    protected XmlSolverBenchmarker solverBenchmarker;

    protected CommonBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        super(LOGGING_CONFIG);
        this.resultFile = resultFile;
        solverBenchmarker = new XmlSolverBenchmarker().configure(solverBenchmarkConfig);
    }

    public void process() {
        solverBenchmarker.benchmark();
        solverBenchmarker.writeResults(resultFile);
    }

}
