package org.drools.solver.core.localsearch.decider.selector;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class CompositeSelector extends AbstractSelector {

    protected List<Selector> selectorList;

    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    @Override
    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        super.setLocalSearchSolver(localSearchSolver);
        for (Selector selector : selectorList) {
            selector.setLocalSearchSolver(localSearchSolver);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        for (Selector selector : selectorList) {
            selector.solvingStarted(localSearchSolverScope);
        }
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        for (Selector selector : selectorList) {
            selector.beforeDeciding(stepScope);
        }
    }

    @Override
    public List<Move> selectMoveList(StepScope stepScope) {
        int totalSize = 0;
        List<List<Move>> subMoveLists = new ArrayList<List<Move>>(selectorList.size());
        for (Selector selector : selectorList) {
            List<Move> subMoveList = selector.selectMoveList(stepScope);
            totalSize += subMoveList.size();
            subMoveLists.add(subMoveList);
        }
        List<Move> moveList = new ArrayList<Move>(totalSize);
        for (List<Move> subMoveList : subMoveLists) {
            moveList.addAll(subMoveList);
        }
        // TODO support overal shuffling
        return moveList;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        for (Selector selector : selectorList) {
            selector.stepDecided(stepScope);
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        for (Selector selector : selectorList) {
            selector.stepTaken(stepScope);
        }
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        for (Selector selector : selectorList) {
            selector.solvingEnded(localSearchSolverScope);
        }
    }

}