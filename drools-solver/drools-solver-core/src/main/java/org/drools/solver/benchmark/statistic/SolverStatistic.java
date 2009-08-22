package org.drools.solver.benchmark.statistic;

import java.io.File;

import org.drools.solver.core.Solver;

/**
 * TODO is this the correct package? Statistics can maybe be used outside the benchmarker
 * @author Geoffrey De Smet
 */
public interface SolverStatistic {

    void addListener(Solver solver);

    void addListener(Solver solver, String configName);

    void writeStatistic(File solverStatisticFilesDirectory, String baseName);

}
