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
        Collections.sort(aScoreList);
        List<Score> bScoreList = b.getScoreList();
        Collections.sort(bScoreList);
        return new CompareToBuilder()
                .append(bScoreList.toArray(), aScoreList.toArray()) // Descending (start with the worst scores)
                .toComparison();
    }

}
