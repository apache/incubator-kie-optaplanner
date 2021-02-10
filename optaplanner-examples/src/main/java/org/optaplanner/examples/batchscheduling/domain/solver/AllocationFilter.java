package org.optaplanner.examples.batchscheduling.domain.solver;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;
import org.optaplanner.examples.batchscheduling.domain.Schedule;

public class AllocationFilter implements SelectionFilter<Schedule, Allocation> {

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        return accept((Schedule) scoreDirector.getWorkingSolution(), allocation);
    }

    public boolean accept(Schedule schedule, Allocation allocation) {

        if (schedule.getAllocationPathList() != null) {
            for (AllocationPath allocationPath : schedule.getAllocationPathList()) {
                if (allocationPath.getRoutePath() != null) {
                    if (allocation.getBatch().getName().equals(allocationPath.getBatch().getName())) {
                        if (allocation.getRoutePath().getPath().equals(allocationPath.getRoutePath().getPath())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
