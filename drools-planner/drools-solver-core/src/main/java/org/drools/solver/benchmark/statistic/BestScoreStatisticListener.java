package org.drools.solver.benchmark.statistic;

import java.util.List;
import java.util.ArrayList;

import org.drools.solver.core.event.BestSolutionChangedEvent;
import org.drools.solver.core.event.SolverEventListener;

/**
 * @author Geoffrey De Smet
 */
public class BestScoreStatisticListener implements SolverEventListener {

    private List<BestScoreStatisticPoint> bestScoreStatisticPointList
            = new ArrayList<BestScoreStatisticPoint>();

    public void bestSolutionChanged(BestSolutionChangedEvent event) {
        bestScoreStatisticPointList.add(new BestScoreStatisticPoint(event));
    }

    public List<BestScoreStatisticPoint> getBestScoreStatisticPointList() {
        return bestScoreStatisticPointList;
    }

}
