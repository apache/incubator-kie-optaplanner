package org.drools.solver.examples.common.app;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.drools.solver.benchmark.XmlSolverBenchmarker;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonBenchmarkApp {

    private static final String LOGGING_CONFIG = "/org/drools/solver/examples/common/app/log4j-benchmark.xml";

    protected XmlSolverBenchmarker solverBenchmarker;
    private String solverBenchmarkConfig;
    private File resultFile;

    protected CommonBenchmarkApp(String solverBenchmarkConfig, File resultFile) {
        this.solverBenchmarkConfig = solverBenchmarkConfig;
        this.resultFile = resultFile;
        DOMConfigurator.configure(getClass().getResource(LOGGING_CONFIG));
         // Workaround to make sure logging reports uncaught exceptions
        LoggerFactory.getLogger(getClass()).debug("Logging configured.");
        solverBenchmarker = new XmlSolverBenchmarker().configure(getSolverBenchmarkConfig());
    }

    public void process() {
        solverBenchmarker.benchmark();
        solverBenchmarker.writeResults(getResultFile());
    }

    protected String getSolverBenchmarkConfig() {
        return solverBenchmarkConfig;
    }

    protected File getResultFile() {
        return resultFile;
    }

}
