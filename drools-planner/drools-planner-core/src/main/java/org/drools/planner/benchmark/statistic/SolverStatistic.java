package org.drools.planner.benchmark.statistic;

import java.io.File;

import org.drools.planner.core.Solver;

/**
 * TODO is this the correct package? Statistics can maybe be used outside the benchmarker
 * @author Geoffrey De Smet
 */
public interface SolverStatistic {

    void addListener(Solver solver);

    void addListener(Solver solver, String configName);

    void removeListener(Solver solver);

    void removeListener(Solver solver, String configName);

    void writeStatistic(File solverStatisticFilesDirectory, String baseName);

}
