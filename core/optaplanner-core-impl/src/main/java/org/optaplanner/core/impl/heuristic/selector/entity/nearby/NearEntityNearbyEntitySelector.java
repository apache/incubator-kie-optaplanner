package org.optaplanner.core.impl.heuristic.selector.entity.nearby;

import java.util.Iterator;
import java.util.ListIterator;

import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbySelector;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;

public final class NearEntityNearbyEntitySelector<Solution_>
        extends AbstractNearbySelector<Solution_, EntitySelector<Solution_>, EntitySelector<Solution_>>
        implements EntitySelector<Solution_> {

    // TODO deactivate me when appropriate; consider if this field needs to be included in selector equality
    private final boolean discardNearbyIndexZero = true;

    public NearEntityNearbyEntitySelector(EntitySelector<Solution_> childEntitySelector,
            EntitySelector<Solution_> originEntitySelector, NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom, boolean randomSelection) {
        super(childEntitySelector, originEntitySelector, nearbyDistanceMeter, nearbyRandom, randomSelection);
        if (!childEntitySelector.getEntityDescriptor().getEntityClass().isAssignableFrom(
                originEntitySelector.getEntityDescriptor().getEntityClass())) {
            throw new IllegalArgumentException("The entitySelector (" + this
                    + ") has an entityClass ("
                    + childEntitySelector.getEntityDescriptor().getEntityClass()
                    + ") which is not a superclass of the originEntitySelector's entityClass ("
                    + originEntitySelector.getEntityDescriptor().getEntityClass() + ").");
        }
    }

    @Override
    protected EntitySelector<Solution_> castReplayingSelector(Object uncastReplayingSelector) {
        if (!(uncastReplayingSelector instanceof MimicReplayingEntitySelector)) {
            // In order to select a nearby entity, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby entity selector (" + this +
                    ") did not receive a replaying entity selector (" + uncastReplayingSelector + ").");
        }
        return (EntitySelector<Solution_>) uncastReplayingSelector;
    }

    @Override
    protected AbstractNearbyDistanceMatrixDemand<?, ?, ?, ?> createDemand() {
        return new EntityNearbyDistanceMatrixDemand<>(
                nearbyDistanceMeter,
                nearbyRandom,
                childSelector,
                replayingSelector,
                origin -> computeDestinationSize(childSelector.getSize()));
    }

    private int computeDestinationSize(long childSize) {
        int destinationSize = (int) childSize;
        if (randomSelection) {
            // Reduce RAM memory usage by reducing destinationSize if nearbyRandom will never select a higher value
            int overallSizeMaximum = nearbyRandom.getOverallSizeMaximum();
            if (discardNearbyIndexZero) {
                if (overallSizeMaximum != Integer.MAX_VALUE) {
                    overallSizeMaximum++;
                }
            }
            if (destinationSize > overallSizeMaximum) {
                destinationSize = overallSizeMaximum;
            }
        }
        return destinationSize;
    }

    @Override
    public EntityDescriptor<Solution_> getEntityDescriptor() {
        return childSelector.getEntityDescriptor();
    }

    @Override
    public boolean isCountable() {
        return true;
    }

    @Override
    public long getSize() {
        return childSelector.getSize() - (discardNearbyIndexZero ? 1 : 0);
    }

    @Override
    public Iterator<Object> iterator() {
        Iterator<Object> replayingOriginEntityIterator = replayingSelector.iterator();
        if (!randomSelection) {
            return new OriginalNearbyEntityIterator(replayingOriginEntityIterator, childSelector.getSize());
        } else {
            return new RandomNearbyEntityIterator(replayingOriginEntityIterator, childSelector.getSize());
        }
    }

    @Override
    public ListIterator<Object> listIterator() {
        // TODO Not yet implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        // TODO Not yet implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Object> endingIterator() {
        // TODO It should probably use nearby order
        // It must include the origin entity too
        return childSelector.endingIterator();
    }

    private final class OriginalNearbyEntityIterator extends SelectionIterator<Object> {

        private final Iterator<Object> replayingOriginEntityIterator;
        private final long childSize;
        private int nextNearbyIndex;

        public OriginalNearbyEntityIterator(Iterator<Object> replayingOriginEntityIterator, long childSize) {
            this.replayingOriginEntityIterator = replayingOriginEntityIterator;
            this.childSize = childSize;
            nextNearbyIndex = discardNearbyIndexZero ? 1 : 0;
        }

        @Override
        public boolean hasNext() {
            return replayingOriginEntityIterator.hasNext() && nextNearbyIndex < childSize;
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
            Object origin = replayingOriginEntityIterator.next();
            Object next = nearbyDistanceMatrixSupply.read().getDestination(origin, nextNearbyIndex);
            nextNearbyIndex++;
            return next;
        }

    }

    private final class RandomNearbyEntityIterator extends SelectionIterator<Object> {

        private final Iterator<Object> replayingOriginEntityIterator;
        private final int nearbySize;

        public RandomNearbyEntityIterator(Iterator<Object> replayingOriginEntityIterator, long childSize) {
            this.replayingOriginEntityIterator = replayingOriginEntityIterator;
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The valueSelector (" + this
                        + ") has an entitySize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            nearbySize = (int) childSize - (discardNearbyIndexZero ? 1 : 0);
        }

        @Override
        public boolean hasNext() {

            return replayingOriginEntityIterator.hasNext() && nearbySize > 0;
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
            Object origin = replayingOriginEntityIterator.next();
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            if (discardNearbyIndexZero) {
                nearbyIndex++;
            }
            return nearbyDistanceMatrixSupply.read().getDestination(origin, nearbyIndex);
        }

    }

}
