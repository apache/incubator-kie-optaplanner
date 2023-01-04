package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Objects;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

/**
 * Exists so that selection filter instances can be compared for equality,
 * in turn enabling equality checks for entity selectors.
 * If two instances share the same selection filter class, they are considered the same selection filter.
 */
final class WrappedSelectionFilter<Solution_, T> implements SelectionFilter<Solution_, T> {

    private final SelectionFilter<Solution_, T> selectionFilter;

    WrappedSelectionFilter(SelectionFilter<Solution_, T> selectionFilter) {
        this.selectionFilter = selectionFilter;
    }

    @Override
    public boolean accept(ScoreDirector<Solution_> scoreDirector, T selection) {
        return selectionFilter.accept(scoreDirector, selection);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        WrappedSelectionFilter<?, ?> that = (WrappedSelectionFilter<?, ?>) other;
        return Objects.equals(selectionFilter.getClass(), that.selectionFilter.getClass());
    }

    @Override
    public int hashCode() {
        return selectionFilter.getClass().hashCode();
    }
}
