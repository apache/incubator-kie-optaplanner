package org.optaplanner.core.impl.heuristic.selector.entity;

import java.util.Comparator;
import java.util.Objects;

/**
 * Exists so that comparator instances can be compared for equality,
 * in turn enabling equality checks for entity selectors.
 * If two instances share the same comparator class, they are considered the same comparator.
 */
public final class WrappedComparator<T> implements Comparator<T> {

    private final Comparator<T> comparator;

    public WrappedComparator() {
        this.comparator = (Comparator<T>) Comparator.naturalOrder();
    }

    public WrappedComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WrappedComparator<?> that = (WrappedComparator<?>) o;
        return Objects.equals(comparator.getClass(), that.comparator.getClass());
    }

    @Override
    public int hashCode() {
        return comparator.getClass().hashCode();
    }
}
