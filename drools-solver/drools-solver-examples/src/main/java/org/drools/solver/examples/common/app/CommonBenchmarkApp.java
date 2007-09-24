package org.drools.solver.examples.common.app;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.drools.solver.benchmark.XmlSolverBenchmarker;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonBenchmarkApp {

    private static final String LOGGING_CONFIG = "/org/drools/solver/examples/common/app/log4j-benchmark.xml";

    protected XmlSolverBenchmarker solverBenchmarker;

    public CommonBenchmarkApp() {
        DOMConfigurator.configure(getClass().getResource(LOGGING_CONFIG));
        solverBenchmarker = new XmlSolverBenchmarker().configure(getSolverBenchmarkConfig());
    }

    public void process() {
        solverBenchmarker.benchmark();
        solverBenchmarker.writeResults(getResultFile());
    }

    protected abstract String getSolverBenchmarkConfig();

    protected abstract File getResultFile();

}
