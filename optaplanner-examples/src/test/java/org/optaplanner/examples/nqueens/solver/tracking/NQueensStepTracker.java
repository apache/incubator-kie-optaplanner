package org.optaplanner.examples.nqueens.solver.tracking;


import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.testdata.util.listeners.StepTestListener;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;

import java.util.ArrayList;
import java.util.List;

public class NQueensStepTracker extends StepTestListener {

    private NQueens lastTrackedPlanningProblem;
    private List<NQueensStepTracking> trackingList = new ArrayList<NQueensStepTracking>();

    public NQueensStepTracker(NQueens lastTrackedPlanningProblem) {
        this.lastTrackedPlanningProblem = lastTrackedPlanningProblem;
    }

    @Override
    public void stepEnded(AbstractStepScope stepScope) {
        NQueens queens = (NQueens) stepScope.getWorkingSolution();

        for (int i = 0; i < queens.getQueenList().size(); i++) {
            Queen queen1 = queens.getQueenList().get(i);
            Queen queen2 = lastTrackedPlanningProblem.getQueenList().get(i);
            if(queen1.getRowIndex() != queen2.getRowIndex()) {
                trackingList.add(new NQueensStepTracking(queen1.getColumnIndex(), queen1.getRowIndex()));
                break;
            }
        }
        lastTrackedPlanningProblem = (NQueens) stepScope.createOrGetClonedSolution();
    }

    public List<NQueensStepTracking> getTrackingList() {
        return trackingList;
    }

}
