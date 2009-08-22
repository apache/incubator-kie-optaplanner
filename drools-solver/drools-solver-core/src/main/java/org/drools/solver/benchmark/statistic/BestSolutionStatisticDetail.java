package org.drools.solver.benchmark.statistic;

import org.drools.solver.core.event.BestSolutionChangedEvent;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class BestSolutionStatisticDetail {

    private long timeMillisSpend;
    private Score score;

    public BestSolutionStatisticDetail(BestSolutionChangedEvent event) {
        timeMillisSpend = event.getTimeMillisSpend();
        score = event.getNewBestSolution().getScore();
    }

    public long getTimeMillisSpend() {
        return timeMillisSpend;
    }

    public Score getScore() {
        return score;
    }

}
