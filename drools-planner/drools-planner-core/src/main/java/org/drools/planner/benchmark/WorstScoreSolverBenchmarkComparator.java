package org.drools.planner.benchmark;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class WorstScoreSolverBenchmarkComparator implements Comparator<SolverBenchmark> {

    public int compare(SolverBenchmark a, SolverBenchmark b) {
        List<Score> aScoreList = a.getScoreList();
        Collections.sort(aScoreList); // Worst scores become first in the list
        List<Score> bScoreList = b.getScoreList();
        Collections.sort(bScoreList); // Worst scores become first in the list
        return new CompareToBuilder()
                .append(aScoreList.toArray(), bScoreList.toArray())
                .toComparison();
    }

}
