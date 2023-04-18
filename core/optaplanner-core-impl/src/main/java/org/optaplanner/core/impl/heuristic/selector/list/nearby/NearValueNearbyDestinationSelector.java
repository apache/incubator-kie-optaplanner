package org.optaplanner.core.impl.heuristic.selector.list.nearby;

import java.util.Iterator;

import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.list.DestinationSelector;
import org.optaplanner.core.impl.heuristic.selector.list.ElementDestinationSelector;
import org.optaplanner.core.impl.heuristic.selector.list.ElementRef;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;

public final class NearValueNearbyDestinationSelector<Solution_>
        extends AbstractNearbyDestinationSelector<Solution_, MimicReplayingValueSelector<Solution_>>
        implements DestinationSelector<Solution_> {

    public NearValueNearbyDestinationSelector(ElementDestinationSelector<Solution_> childDestinationSelector,
            EntityIndependentValueSelector<Solution_> originValueSelector, NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom, boolean randomSelection) {
        super(childDestinationSelector, originValueSelector, nearbyDistanceMeter, nearbyRandom, randomSelection);
    }

    @Override
    protected MimicReplayingValueSelector<Solution_> castReplayingSelector(Object uncastReplayingSelector) {
        if (!(uncastReplayingSelector instanceof MimicReplayingValueSelector)) {
            // In order to select a nearby destination, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby destination selector (" + this +
                    ") did not receive a replaying value selector (" + uncastReplayingSelector + ").");
        }
        return (MimicReplayingValueSelector<Solution_>) uncastReplayingSelector;
    }

    @Override
    protected AbstractNearbyDistanceMatrixDemand<?, ?, ?, ?> createDemand() {
        return new ListNearbyDistanceMatrixDemand<>(
                nearbyDistanceMeter,
                nearbyRandom,
                childSelector,
                replayingSelector,
                this::computeDestinationSize);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public Iterator<ElementRef> iterator() {
        Iterator<Object> replayingOriginValueIterator = replayingSelector.iterator();
        if (!randomSelection) {
            return new OriginalValueNearbyDestinationIterator(replayingOriginValueIterator, childSelector.getSize());
        } else {
            return new RandomValueNearbyDestinationIterator(replayingOriginValueIterator, childSelector.getSize());
        }
    }

    private final class OriginalValueNearbyDestinationIterator extends SelectionIterator<ElementRef> {

        private final Iterator<Object> replayingOriginValueIterator;
        private final long childSize;

        private boolean originSelected = false;
        private boolean originIsNotEmpty;
        private Object origin;

        private int nextNearbyIndex;

        public OriginalValueNearbyDestinationIterator(Iterator<Object> replayingOriginValueIterator, long childSize) {
            this.replayingOriginValueIterator = replayingOriginValueIterator;
            this.childSize = childSize;
            nextNearbyIndex = 0;
        }

        private void selectOrigin() {
            if (originSelected) {
                return;
            }
            /*
             * The origin iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, origin here will be constant unless next() on the original recording iterator is called
             * first.
             */
            originIsNotEmpty = replayingOriginValueIterator.hasNext();
            origin = replayingOriginValueIterator.next();
            originSelected = true;
        }

        @Override
        public boolean hasNext() {
            selectOrigin();
            return originIsNotEmpty && nextNearbyIndex < childSize;
        }

        @Override
        public ElementRef next() {
            selectOrigin();
            Object next = nearbyDistanceMatrix.getDestination(origin, nextNearbyIndex);
            nextNearbyIndex++;
            return elementRef(next);
        }

    }

    private final class RandomValueNearbyDestinationIterator extends SelectionIterator<ElementRef> {

        private final Iterator<Object> replayingOriginValueIterator;
        private final int nearbySize;

        public RandomValueNearbyDestinationIterator(Iterator<Object> replayingOriginValueIterator, long childSize) {
            this.replayingOriginValueIterator = replayingOriginValueIterator;
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The destinationSelector (" + this
                        + ") has a destinationSize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            nearbySize = (int) childSize;
        }

        @Override
        public boolean hasNext() {
            return replayingOriginValueIterator.hasNext() && nearbySize > 0;
        }

        @Override
        public ElementRef next() {
            /*
             * The origin iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, origin here will be constant unless next() on the original recording iterator is called
             * first.
             */
            Object origin = replayingOriginValueIterator.next();
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            Object next = nearbyDistanceMatrix.getDestination(origin, nearbyIndex);
            return elementRef(next);
        }

    }

}
