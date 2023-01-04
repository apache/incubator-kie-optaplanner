package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorter;

/**
 * Exists so that sorters can be compared for equality,
 * in turn enabling equality checks for entity selectors.
 * If two instances share the same sorter class, they are considered the same sorter.
 */
final class WrappedSelectionSorter<Solution_, T> implements SelectionSorter<Solution_, T> {

    private final SelectionSorter<Solution_, T> selectionSorter;

    WrappedSelectionSorter(SelectionSorter<Solution_, T> selectionSorter) {
        this.selectionSorter = selectionSorter;
    }

    @Override
    public void sort(ScoreDirector<Solution_> scoreDirector, List<T> selectionList) {
        selectionSorter.sort(scoreDirector, selectionList);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        WrappedSelectionSorter<?, ?> that = (WrappedSelectionSorter<?, ?>) other;
        return Objects.equals(selectionSorter.getClass(), that.selectionSorter.getClass());
    }

    @Override
    public int hashCode() {
        return selectionSorter.getClass().hashCode();
    }
}
