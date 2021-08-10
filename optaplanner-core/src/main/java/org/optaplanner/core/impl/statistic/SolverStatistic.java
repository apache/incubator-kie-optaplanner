package org.optaplanner.core.impl.statistic;

import org.optaplanner.core.api.solver.Solver;

public interface SolverStatistic {
    void register(Solver<?> solver);
}
