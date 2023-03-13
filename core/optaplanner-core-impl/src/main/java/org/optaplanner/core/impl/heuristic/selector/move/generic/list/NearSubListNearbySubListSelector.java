package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;
import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicReplayingSubListSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.util.MemoizingSupply;

public final class NearSubListNearbySubListSelector<Solution_> extends AbstractSelector<Solution_>
        implements SubListSelector<Solution_> {

    private final RandomSubListSelector<Solution_> childSubListSelector;
    private final MimicReplayingSubListSelector<Solution_> replayingOriginSubListSelector;
    private final NearbyDistanceMeter<?, ?> nearbyDistanceMeter;
    private final NearbyRandom nearbyRandom;
    private final boolean randomSelection;
    private final SubListNearbySubListMatrixDemand<Solution_, ?, ?> nearbyDistanceMatrixDemand;

    private MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply = null;
    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    public NearSubListNearbySubListSelector(
            RandomSubListSelector<Solution_> childSubListSelector,
            SubListSelector<Solution_> originSubListSelector,
            NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom) {
        this.childSubListSelector = childSubListSelector;
        if (!(originSubListSelector instanceof MimicReplayingSubListSelector)) {
            // In order to select a nearby subList, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby subList selector (" + this +
                    ") did not receive a replaying subList selector (" + originSubListSelector + ").");
        }
        this.replayingOriginSubListSelector = (MimicReplayingSubListSelector<Solution_>) originSubListSelector;
        this.nearbyDistanceMeter = nearbyDistanceMeter;
        this.nearbyRandom = nearbyRandom;
        this.randomSelection = true;
        if (randomSelection && nearbyRandom == null) {
            throw new IllegalArgumentException("The subListSelector (" + this
                    + ") with randomSelection (" + randomSelection + ") has no nearbyRandom (" + nearbyRandom + ").");
        }
        this.nearbyDistanceMatrixDemand = new SubListNearbySubListMatrixDemand<>(
                nearbyDistanceMeter,
                childSubListSelector,
                replayingOriginSubListSelector,
                this::computeDestinationSize);

        phaseLifecycleSupport.addEventListener(childSubListSelector);
        phaseLifecycleSupport.addEventListener(originSubListSelector);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        ListVariableDescriptor<Solution_> listVariableDescriptor = childSubListSelector.getVariableDescriptor();
        /*
         * Supply will ask questions of the child selector.
         * However, child selector will only be initialized during phase start.
         * Yet we still want the very expensive nearby distance matrix to be reused across phases.
         * Therefore we request the supply here, but actually lazily initialize it during phase start.
         */
        nearbyDistanceMatrixSupply = (MemoizingSupply) supplyManager.demand(nearbyDistanceMatrixDemand);
        inverseVariableSupply = supplyManager.demand(new SingletonListInverseVariableDemand<>(listVariableDescriptor));
        indexVariableSupply = supplyManager.demand(new IndexVariableDemand<>(listVariableDescriptor));
    }

    @Override
    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        // Lazily initialize the supply, so that steps can then have uniform performance.
        nearbyDistanceMatrixSupply.read();
    }

    private int computeDestinationSize(Object origin) {
        long childSize = childSubListSelector.getValueCount();
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The childSubListSelector (" + childSubListSelector
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

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        solverScope.getScoreDirector().getSupplyManager().cancel(nearbyDistanceMatrixDemand);
        nearbyDistanceMatrixSupply = null;
        inverseVariableSupply = null;
        indexVariableSupply = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return childSubListSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || !isCountable();
    }

    @Override
    public long getSize() {
        return childSubListSelector.getSize();
    }

    @Override
    public Iterator<SubList> iterator() {
        Iterator<SubList> replayingOriginSubListIterator = replayingOriginSubListSelector.iterator();
        if (!randomSelection) {
            throw new IllegalStateException("This selector only supports random selection.");
        } else {
            return new RandomSubListNearbySubListIterator(replayingOriginSubListIterator,
                    childSubListSelector.getValueCount());
        }
    }

    private final class RandomSubListNearbySubListIterator extends SelectionIterator<SubList> {

        private final Iterator<SubList> replayingOriginSubListIterator;
        private final int nearbySize;

        public RandomSubListNearbySubListIterator(Iterator<SubList> replayingOriginSubListIterator, long childSize) {
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
            // As long as the origin selector has next and the nearby index is non-empty, we have next. Nearby index size
            // is the child selector size. Child selector's iterator is not used.
            return replayingOriginSubListIterator.hasNext() && nearbySize > 0;
        }

        @Override
        public SubList next() {
            /*
             * The origin iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, subList here will be constant unless next() on the original recording iterator is called
             * first.
             */
            SubList subList = replayingOriginSubListIterator.next();
            // Origin is the subList's first element.
            Object origin = firstElement(subList);

            // Next steps:
            // 1. Select a destination near to the origin based on a random nearby index.
            // 2. Select a random subList with destination being its first element.

            Object nearbyElementEntity = null;
            Integer nearbyElementListIndex = -1;
            int availableListSize = -1;

            // TODO What if MIN is 500? We could burn thousands of cycles before we hit a availableListSize >= 500!
            while (availableListSize < childSubListSelector.getMinimumSubListSize()) {
                int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
                Object nearbyElement = nearbyDistanceMatrixSupply.read().getDestination(origin, nearbyIndex);
                nearbyElementEntity = inverseVariableSupply.getInverseSingleton(nearbyElement);
                nearbyElementListIndex = indexVariableSupply.getIndex(nearbyElement);
                // Reduce the list variable size by the nearby element index because we're only going to select subLists
                // starting with the nearby element.
                availableListSize = listSize(nearbyElementEntity) - nearbyElementListIndex;
            }

            int maxSubListSize = Math.min(childSubListSelector.getMaximumSubListSize(), availableListSize);
            int subListSizeRange = maxSubListSize - childSubListSelector.getMinimumSubListSize();

            int subListSize = workingRandom.nextInt(subListSizeRange) + childSubListSelector.getMinimumSubListSize();

            return new SubList(nearbyElementEntity, nearbyElementListIndex, subListSize);
        }

    }

    private Object firstElement(SubList subList) {
        return replayingOriginSubListSelector.getVariableDescriptor().getElement(subList.getEntity(), subList.getFromIndex());
    }

    private int listSize(Object entity) {
        return childSubListSelector.getVariableDescriptor().getListSize(entity);
    }

    @Override
    public ListVariableDescriptor<Solution_> getVariableDescriptor() {
        throw new UnsupportedOperationException("Not used.");
    }

    @Override
    public Iterator<Object> endingValueIterator() {
        throw new UnsupportedOperationException("Not used.");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        NearSubListNearbySubListSelector<?> that = (NearSubListNearbySubListSelector<?>) other;
        return randomSelection == that.randomSelection
                && Objects.equals(childSubListSelector, that.childSubListSelector)
                && Objects.equals(replayingOriginSubListSelector, that.replayingOriginSubListSelector)
                && Objects.equals(nearbyDistanceMeter, that.nearbyDistanceMeter)
                && Objects.equals(nearbyRandom, that.nearbyRandom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childSubListSelector, replayingOriginSubListSelector, nearbyDistanceMeter, nearbyRandom,
                randomSelection);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + replayingOriginSubListSelector + ", " + childSubListSelector + ")";
    }
}
