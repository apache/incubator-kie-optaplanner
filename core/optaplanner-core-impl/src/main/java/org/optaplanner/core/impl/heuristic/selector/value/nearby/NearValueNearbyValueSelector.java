package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import java.util.Iterator;
import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.util.MemoizingSupply;

public final class NearValueNearbyValueSelector<Solution_>
        extends
        AbstractNearbyValueSelector<Solution_, EntityIndependentValueSelector<Solution_>, MimicReplayingValueSelector<Solution_>>
        implements EntityIndependentValueSelector<Solution_> {

    public NearValueNearbyValueSelector(
            EntityIndependentValueSelector<Solution_> childValueSelector,
            EntityIndependentValueSelector<Solution_> originValueSelector,
            NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom, boolean randomSelection) {
        super(childValueSelector, originValueSelector, nearbyDistanceMeter, nearbyRandom, randomSelection);
    }

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return childSelector.getVariableDescriptor();
    }

    @Override
    protected MimicReplayingValueSelector<Solution_> castReplayingSelector(Object uncastReplayingSelector) {
        if (!(uncastReplayingSelector instanceof MimicReplayingValueSelector)) {
            // In order to select a nearby value, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby value selector (" + this +
                    ") did not receive a replaying value selector (" + uncastReplayingSelector + ").");
        }
        return (MimicReplayingValueSelector<Solution_>) uncastReplayingSelector;
    }

    @Override
    protected
            AbstractNearbyDistanceMatrixDemand<?, ?, EntityIndependentValueSelector<Solution_>, MimicReplayingValueSelector<Solution_>>
            createDemand() {
        return new ListValueNearbyDistanceMatrixDemand<>(
                nearbyDistanceMeter,
                nearbyRandom,
                childSelector,
                replayingSelector,
                this::computeDestinationSize);
    }

    private int computeDestinationSize(Object origin) {
        long childSize = childSelector.getSize();
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The childValueSelector (" + childSelector
                    + ") has a valueSize (" + childSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }

        int destinationSize = (int) childSize;
        if (randomSelection) {
            // Reduce RAM memory usage by reducing destinationSize if nearbyRandom will never select a higher value
            int overallSizeMaximum = nearbyRandom.getOverallSizeMaximum();
            if (destinationSize > overallSizeMaximum) {
                destinationSize = overallSizeMaximum;
            }
        }
        return destinationSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return childSelector.isCountable();
    }

    @Override
    public long getSize(Object entity) {
        return getSize();
    }

    @Override
    public long getSize() {
        return childSelector.getSize();
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        return iterator();
    }

    @Override
    public Iterator<Object> iterator() {
        Iterator<Object> replayingOriginValueIterator = replayingSelector.iterator();
        if (!randomSelection) {
            return new OriginalNearbyValueIterator(replayingOriginValueIterator, childSelector.getSize());
        } else {
            return new RandomNearbyValueIterator(replayingOriginValueIterator, childSelector.getSize());
        }
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        // TODO It should probably use nearby order
        // It must include the origin entity too
        return childSelector.endingIterator(entity);
    }

    private final class OriginalNearbyValueIterator extends SelectionIterator<Object> {

        private final Iterator<Object> replayingOriginValueIterator;
        private final long childSize;

        private boolean originSelected = false;
        private boolean originIsNotEmpty;
        private Object origin;

        private int nextNearbyIndex;

        public OriginalNearbyValueIterator(Iterator<Object> replayingOriginValueIterator, long childSize) {
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
        public Object next() {
            selectOrigin();
            Object next = nearbyDistanceMatrixSupply.read().getDestination(origin, nextNearbyIndex);
            nextNearbyIndex++;
            return next;
        }

    }

    private final class RandomNearbyValueIterator extends SelectionIterator<Object> {

        private final Iterator<Object> replayingOriginValueIterator;
        private final int nearbySize;

        public RandomNearbyValueIterator(Iterator<Object> replayingOriginValueIterator, long childSize) {
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
        public Object next() {
            /*
             * The origin iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, origin here will be constant unless next() on the original recording iterator is called
             * first.
             */
            Object origin = replayingOriginValueIterator.next();
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            return nearbyDistanceMatrixSupply.read().getDestination(origin, nearbyIndex);
        }

    }

}
