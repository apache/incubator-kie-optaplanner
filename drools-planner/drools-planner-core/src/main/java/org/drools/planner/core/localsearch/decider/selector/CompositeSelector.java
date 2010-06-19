package org.drools.planner.core.localsearch.decider.selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.Decider;
import org.drools.planner.core.move.Move;

/**
 * A CompositeSelector unions multiple Selectors.
 * @author Geoffrey De Smet
 */
public class CompositeSelector extends AbstractSelector {

    protected List<Selector> selectorList;

    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    @Override
    public void setDecider(Decider decider) {
        super.setDecider(decider);
        for (Selector selector : selectorList) {
            selector.setDecider(decider);
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

    public Iterator<Move> moveIterator(StepScope stepScope) {
        List<Iterator<Move>> moveIteratorList = new ArrayList<Iterator<Move>>(selectorList.size());
        for (Selector selector : selectorList) {
            Iterator<Move> moveIterator = selector.moveIterator(stepScope);
            if (moveIterator.hasNext()) {
                moveIteratorList.add(moveIterator);
            }
        }
        return new CompositeSelectorMoveIterator(stepScope.getWorkingRandom(), moveIteratorList);
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

    private static class CompositeSelectorMoveIterator implements Iterator<Move> {

        private final List<Iterator<Move>> moveIteratorList;
        private final Random workingRandom;

        public CompositeSelectorMoveIterator(Random workingRandom, List<Iterator<Move>> moveIteratorList) {
            this.moveIteratorList = moveIteratorList;
            this.workingRandom = workingRandom;
        }

        public boolean hasNext() {
            return !moveIteratorList.isEmpty();
        }

        public Move next() {
            int moveIteratorIndex = workingRandom.nextInt(moveIteratorList.size());
            Iterator<Move> moveIterator = moveIteratorList.get(moveIteratorIndex);
            Move next = moveIterator.next();
            if (!moveIterator.hasNext()) {
                moveIteratorList.remove(moveIteratorIndex);
            }
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "The remove method is not supported on CompositeSelectorMoveIterator");
        }

    }

}
