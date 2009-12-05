package org.drools.solver.benchmark;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Geoffrey De Smet
 */
public class MaxScoreSolverBenchmarkComparator implements Comparator<SolverBenchmark> {

    public int compare(SolverBenchmark a, SolverBenchmark b) {
        SolverBenchmarkResult aResult = a.getWorstResult();
        SolverBenchmarkResult bResult = b.getWorstResult();
        return new CompareToBuilder()
                .append(aResult.getScore(), bResult.getScore())
                .append(- aResult.getTimeMillesSpend(), - bResult.getTimeMillesSpend())
                .toComparison();
    }

}
