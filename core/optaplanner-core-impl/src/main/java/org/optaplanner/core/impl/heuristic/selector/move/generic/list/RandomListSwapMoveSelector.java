package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.AbstractRandomSwapIterator;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

public class RandomListSwapMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final JustInTimeElementSelector<Solution_> leftElementSelector;
    private final JustInTimeElementSelector<Solution_> rightElementSelector;

    public RandomListSwapMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntityIndependentValueSelector<Solution_> leftValueSelector,
            EntityIndependentValueSelector<Solution_> rightValueSelector) {
        this.listVariableDescriptor = listVariableDescriptor;
        leftElementSelector = new JustInTimeElementSelector<>(leftValueSelector);
        rightElementSelector = new JustInTimeElementSelector<>(rightValueSelector);
        phaseLifecycleSupport.addEventListener(leftElementSelector);
        phaseLifecycleSupport.addEventListener(rightElementSelector);
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new AbstractRandomSwapIterator<>(leftElementSelector, rightElementSelector) {
            @Override
            protected Move<Solution_> newSwapSelection(ElementRef leftElement, ElementRef rightElement) {
                return new ListSwapMove<>(listVariableDescriptor, leftElement, rightElement);
            }
        };
    }

    @Override
    public boolean isCountable() {
        return leftElementSelector.isCountable() && rightElementSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        // Random selector is never ending.
        return true;
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Random selector does not have a size.");
    }
}
