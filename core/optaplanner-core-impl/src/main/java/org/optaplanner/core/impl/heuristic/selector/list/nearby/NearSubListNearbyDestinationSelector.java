package org.optaplanner.core.impl.heuristic.selector.list.nearby;

import java.util.Iterator;

import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.list.DestinationSelector;
import org.optaplanner.core.impl.heuristic.selector.list.ElementDestinationSelector;
import org.optaplanner.core.impl.heuristic.selector.list.ElementRef;
import org.optaplanner.core.impl.heuristic.selector.list.SubList;
import org.optaplanner.core.impl.heuristic.selector.list.SubListSelector;
import org.optaplanner.core.impl.heuristic.selector.list.mimic.MimicReplayingSubListSelector;

public final class NearSubListNearbyDestinationSelector<Solution_>
        extends AbstractNearbyDestinationSelector<Solution_, MimicReplayingSubListSelector<Solution_>>
        implements DestinationSelector<Solution_> {

    public NearSubListNearbyDestinationSelector(ElementDestinationSelector<Solution_> childDestinationSelector, SubListSelector<Solution_> originSubListSelector, NearbyDistanceMeter<?, ?> nearbyDistanceMeter, NearbyRandom nearbyRandom, boolean randomSelection) {
        super(childDestinationSelector, originSubListSelector, nearbyDistanceMeter, nearbyRandom, randomSelection);
    }

    @Override
    protected MimicReplayingSubListSelector<Solution_> castReplayingSelector(Object uncastReplayingSelector) {
        if (!(uncastReplayingSelector instanceof MimicReplayingSubListSelector)) {
            // In order to select a nearby destination, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby destination selector (" + this +
                    ") did not receive a replaying subList selector (" + uncastReplayingSelector + ").");
        }
        return (MimicReplayingSubListSelector<Solution_>) uncastReplayingSelector;
    }

    @Override
    protected AbstractNearbyDistanceMatrixDemand<?, ?, ?, ?> createDemand() {
        return new SubListNearbyDistanceMatrixDemand<>(
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
        Iterator<SubList> replayingOriginSubListIterator = replayingSelector.iterator();
        if (!randomSelection) {
            return new OriginalSubListNearbyDestinationIterator(replayingOriginSubListIterator,
                    childSelector.getSize());
        } else {
            return new RandomSubListNearbyDestinationIterator(replayingOriginSubListIterator,
                    childSelector.getSize());
        }
    }

    private final class OriginalSubListNearbyDestinationIterator extends SelectionIterator<ElementRef> {

        private final Iterator<SubList> replayingOriginSubListIterator;
        private final long childSize;

        private boolean originSelected = false;
        private boolean originIsNotEmpty;
        private Object origin;

        private int nextNearbyIndex;

        public OriginalSubListNearbyDestinationIterator(Iterator<SubList> replayingOriginSubListIterator, long childSize) {
            this.replayingOriginSubListIterator = replayingOriginSubListIterator;
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
            originIsNotEmpty = replayingOriginSubListIterator.hasNext();
            SubList subList = replayingOriginSubListIterator.next();
            // Origin is the subList's first element.
            origin = firstElement(subList);
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

    private final class RandomSubListNearbyDestinationIterator extends SelectionIterator<ElementRef> {

        private final Iterator<SubList> replayingOriginSubListIterator;
        private final int nearbySize;

        public RandomSubListNearbyDestinationIterator(Iterator<SubList> replayingOriginSubListIterator, long childSize) {
            this.replayingOriginSubListIterator = replayingOriginSubListIterator;
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The destinationSelector (" + this
                        + ") has a destinationSize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            nearbySize = (int) childSize;
        }

        @Override
        public boolean hasNext() {
            return replayingOriginSubListIterator.hasNext() && nearbySize > 0;
        }

        @Override
        public ElementRef next() {
            /*
             * The subList iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, subList here will be constant unless next() on the original recording iterator is called
             * first.
             */
            SubList subList = replayingOriginSubListIterator.next();
            // Origin is the subList's first element.
            Object origin = firstElement(subList);
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            Object next = nearbyDistanceMatrix.getDestination(origin, nearbyIndex);
            return elementRef(next);
        }

    }

    private Object firstElement(SubList subList) {
        return replayingSelector.getVariableDescriptor().getElement(subList.getEntity(), subList.getFromIndex());
    }

}
