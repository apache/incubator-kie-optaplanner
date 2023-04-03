package org.optaplanner.core.impl.heuristic.selector.common.decorator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;

/**
 * Combines several {@link SelectionFilter}s into one.
 * Does a logical AND over the accept status of its filters.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <T> the selection type
 */
public final class CompositeSelectionFilter<Solution_, T> implements SelectionFilter<Solution_, T> {

    private static final SelectionFilter NOOP = (scoreDirector, selection) -> true;

    public static <Solution_, T> SelectionFilter<Solution_, T> of(SelectionFilter<Solution_, T>... filterArray) {
        return of(Arrays.asList(filterArray));
    }

    public static <Solution_, T> SelectionFilter<Solution_, T> of(List<SelectionFilter<Solution_, T>> filterList) {
        // Crack the filter list; decompose composites if necessary.
        var distinctFilterList = filterList.stream()
                .flatMap(f -> {
                    if (f instanceof CompositeSelectionFilter) {
                        return Arrays.stream(((CompositeSelectionFilter<Solution_, T>) f).selectionFilterArray);
                    } else {
                        return Stream.of(f);
                    }
                })
                .distinct()
                .collect(Collectors.toList());
        switch (distinctFilterList.size()) {
            case 0:
                return NOOP;
            case 1:
                return distinctFilterList.get(0);
            default:
                return new CompositeSelectionFilter<>(distinctFilterList);
        }
    }

    private final SelectionFilter<Solution_, T>[] selectionFilterArray;

    private CompositeSelectionFilter(List<SelectionFilter<Solution_, T>> selectionFilterList) {
        this.selectionFilterArray = selectionFilterList.toArray(SelectionFilter[]::new);
    }

    @Override
    public boolean accept(ScoreDirector<Solution_> scoreDirector, T selection) {
        for (SelectionFilter<Solution_, T> selectionFilter : selectionFilterArray) {
            if (!selectionFilter.accept(scoreDirector, selection)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        CompositeSelectionFilter<?, ?> that = (CompositeSelectionFilter<?, ?>) other;
        return Arrays.equals(selectionFilterArray, that.selectionFilterArray);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(selectionFilterArray);
    }

}
