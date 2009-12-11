package org.drools.planner.benchmark.statistic;

import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class BestScoreStatisticPoint {

    private long timeMillisSpend;
    private Score score;

    public BestScoreStatisticPoint(BestSolutionChangedEvent event) {
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
