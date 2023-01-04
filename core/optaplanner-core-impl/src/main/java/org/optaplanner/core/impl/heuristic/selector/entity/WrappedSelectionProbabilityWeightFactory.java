package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Objects;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;

/**
 * Exists so that factories can be compared for equality,
 * in turn enabling equality checks for entity selectors.
 * If two instances share the same factory class, they are considered the same factory.
 */
final class WrappedSelectionProbabilityWeightFactory<Solution_, T>
        implements SelectionProbabilityWeightFactory<Solution_, T> {

    private final SelectionProbabilityWeightFactory<Solution_, T> selectionProbabilityWeightFactory;

    WrappedSelectionProbabilityWeightFactory(
            SelectionProbabilityWeightFactory<Solution_, T> selectionProbabilityWeightFactory) {
        this.selectionProbabilityWeightFactory = selectionProbabilityWeightFactory;
    }

    @Override
    public double createProbabilityWeight(ScoreDirector<Solution_> scoreDirector, T selection) {
        return selectionProbabilityWeightFactory.createProbabilityWeight(scoreDirector, selection);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        WrappedSelectionProbabilityWeightFactory<?, ?> that =
                (WrappedSelectionProbabilityWeightFactory<?, ?>) other;
        return Objects.equals(selectionProbabilityWeightFactory.getClass(), that.selectionProbabilityWeightFactory.getClass());
    }

    @Override
    public int hashCode() {
        return selectionProbabilityWeightFactory.getClass().hashCode();
    }

}
