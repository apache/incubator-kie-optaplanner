package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.AbstractOriginalSwapIterator;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

public class OriginalListSwapMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final CachedElementSelector<Solution_> elementSelector;

    public OriginalListSwapMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntityIndependentValueSelector<Solution_> leftValueSelector) {
        this.listVariableDescriptor = listVariableDescriptor;
        elementSelector = new CachedElementSelector<>(leftValueSelector);
        phaseLifecycleSupport.addEventListener(elementSelector);
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new AbstractOriginalSwapIterator<>(elementSelector, elementSelector) {
            @Override
            protected Move<Solution_> newSwapSelection(ElementRef leftElement, ElementRef rightElement) {
                return new ListSwapMove<>(listVariableDescriptor, leftElement, rightElement);
            }
        };
    }

    @Override
    public boolean isCountable() {
        // Because elementSelector is countable.
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        // Because elementSelector isn't never ending.
        return false;
    }

    @Override
    public long getSize() {
        return AbstractOriginalSwapIterator.getSize(elementSelector, elementSelector);
    }
}
