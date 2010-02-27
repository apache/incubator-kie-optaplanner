package org.drools.planner.benchmark.statistic;

import java.io.File;

import org.drools.planner.core.Solver;

/**
 * TODO is this the correct package? Statistics can maybe be used outside the benchmarker
 * @author Geoffrey De Smet
 */
public interface SolverStatistic {

    void addListener(Solver solver, String configName);

    void removeListener(Solver solver, String configName);

    CharSequence writeStatistic(File solverStatisticFilesDirectory, String baseName);

}
