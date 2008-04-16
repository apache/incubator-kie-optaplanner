package org.drools.solver.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.List;

import org.drools.solver.core.localsearch.DefaultLocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class TopListSelector extends AbstractSelector {

    private int topSize;

    private List<Move> topList;

    public void setTopSize(int topSize) {
        this.topSize = topSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        topList = Collections.emptyList();
    }

    @Override
    public final List<Move> selectMoveList(StepScope stepScope) {
        return topList;
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        // TODO fixme
        topList = ((DefaultLocalSearchSolver) localSearchSolver).getDecider().getForager().getTopList(topSize);
    }

}