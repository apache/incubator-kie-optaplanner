package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.util.MemoizingSupply;

final class FixedOriginOriginalNearbyValueIterator extends SelectionIterator<Object> {
    private final Object origin;

    private final long childSize;

    private int nextNearbyIndex;

    private final MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply;

    public FixedOriginOriginalNearbyValueIterator(
            MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply, long childSize, Object origin) {
        this.origin = origin;
        this.childSize = childSize;
        this.nearbyDistanceMatrixSupply = nearbyDistanceMatrixSupply;
        nextNearbyIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return nextNearbyIndex < childSize;
    }

    @Override
    public Object next() {
        Object next = nearbyDistanceMatrixSupply.read().getDestination(origin, nextNearbyIndex);
        nextNearbyIndex++;
        return next;
    }

}
