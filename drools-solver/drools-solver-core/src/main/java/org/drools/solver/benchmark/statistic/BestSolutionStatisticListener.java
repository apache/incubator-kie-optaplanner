package org.drools.solver.benchmark.statistic;

import java.util.List;
import java.util.ArrayList;

import org.drools.solver.core.event.BestSolutionChangedEvent;
import org.drools.solver.core.event.SolverEventListener;

/**
 * @author Geoffrey De Smet
 */
public class BestSolutionStatisticListener implements SolverEventListener {

    private List<BestSolutionStatisticDetail> bestSolutionStatisticDetailList
            = new ArrayList<BestSolutionStatisticDetail>();

    public void bestSolutionChanged(BestSolutionChangedEvent event) {
        bestSolutionStatisticDetailList.add(new BestSolutionStatisticDetail(event));
    }

    public List<BestSolutionStatisticDetail> getBestSolutionStatisticDetailList() {
        return bestSolutionStatisticDetailList;
    }

}
