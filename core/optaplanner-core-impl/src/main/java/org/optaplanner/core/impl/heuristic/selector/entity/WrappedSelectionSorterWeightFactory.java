package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Objects;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

/**
 * Exists so that factories can be compared for equality,
 * in turn enabling equality checks for entity selectors.
 * If two instances share the same factory class, they are considered the same factory.
 */
final class WrappedSelectionSorterWeightFactory<Solution_, T> implements SelectionSorterWeightFactory<Solution_, T> {

    private final SelectionSorterWeightFactory<Solution_, T> selectionSorterWeightFactory;

    WrappedSelectionSorterWeightFactory(SelectionSorterWeightFactory<Solution_, T> selectionSorterWeightFactory) {
        this.selectionSorterWeightFactory = selectionSorterWeightFactory;
    }

    @Override
    public Comparable createSorterWeight(Solution_ solution, T selection) {
        return selectionSorterWeightFactory.createSorterWeight(solution, selection);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        WrappedSelectionSorterWeightFactory<?, ?> that = (WrappedSelectionSorterWeightFactory<?, ?>) other;
        return Objects.equals(selectionSorterWeightFactory.getClass(), that.selectionSorterWeightFactory.getClass());
    }

    @Override
    public int hashCode() {
        return selectionSorterWeightFactory.getClass().hashCode();
    }

}
