package org.drools.planner.benchmark;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class AverageScoreSolverBenchmarkComparator implements Comparator<SolverBenchmark> {

    private WorstScoreSolverBenchmarkComparator worstScoreSolverBenchmarkComparator
            = new WorstScoreSolverBenchmarkComparator();

    public int compare(SolverBenchmark a, SolverBenchmark b) {
        return new CompareToBuilder()
                .append(a.getAverageScore(), b.getAverageScore())
                .append(a, b, worstScoreSolverBenchmarkComparator)
                .toComparison();
    }

}
