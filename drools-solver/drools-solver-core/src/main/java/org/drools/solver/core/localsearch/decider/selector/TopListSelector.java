package org.drools.solver.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.List;

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

    public void solvingStarted() {
        topList = Collections.emptyList();
    }

    public final List<Move> selectMoveList() {
        return topList;
    }

    public void stepTaken() {
        topList = localSearchSolver.getDecider().getForager().getTopList(topSize);
    }

}