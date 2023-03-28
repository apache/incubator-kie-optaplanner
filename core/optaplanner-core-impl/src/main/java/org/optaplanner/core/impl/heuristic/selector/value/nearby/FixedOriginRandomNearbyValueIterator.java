package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import java.util.Random;

import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.util.MemoizingSupply;

final class FixedOriginRandomNearbyValueIterator extends SelectionIterator<Object> {

    private final Object origin;
    private final int nearbySize;

    private final MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply;
    private final NearbyRandom nearbyRandom;
    private final Random workingRandom;

    public FixedOriginRandomNearbyValueIterator(
            MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply, NearbyRandom nearbyRandom,
            Random workingRandom, Object origin, long childSize) {
        this.origin = origin;
        this.nearbyDistanceMatrixSupply = nearbyDistanceMatrixSupply;
        this.nearbyRandom = nearbyRandom;
        this.workingRandom = workingRandom;
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The destinationSelector (" + this
                    + ") has a destinationSize (" + childSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        nearbySize = (int) childSize;
    }

    @Override
    public boolean hasNext() {
        return nearbySize > 0;
    }

    @Override
    public Object next() {
        int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
        return nearbyDistanceMatrixSupply.read().getDestination(origin, nearbyIndex);
    }

}
