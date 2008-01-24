package org.drools.solver.core.localsearch.decider.selector;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class CompositeSelector extends AbstractSelector {

    protected List<Selector> selectorList;

    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        for (Selector selector : selectorList) {
            selector.setLocalSearchSolver(localSearchSolver);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted() {
        for (Selector selector : selectorList) {
            selector.solvingStarted();
        }
    }

    public void beforeDeciding() {
        for (Selector selector : selectorList) {
            selector.beforeDeciding();
        }
    }

    public List<Move> selectMoveList() {
        int totalSize = 0;
        List<List<Move>> subMoveLists = new ArrayList<List<Move>>(selectorList.size());
        for (Selector selector : selectorList) {
            List<Move> subMoveList = selector.selectMoveList();
            totalSize += subMoveList.size();
            subMoveLists.add(subMoveList);
        }
        List<Move> moveList = new ArrayList<Move>(totalSize);
        for (List<Move> subMoveList : subMoveLists) {
            moveList.addAll(subMoveList);
        }
        return moveList;
    }

    public void stepDecided(Move step) {
        for (Selector selector : selectorList) {
            selector.stepDecided(step);
        }
    }

    public void stepTaken() {
        for (Selector selector : selectorList) {
            selector.stepTaken();
        }
    }

    public void solvingEnded() {
        for (Selector selector : selectorList) {
            selector.solvingEnded();
        }
    }

}