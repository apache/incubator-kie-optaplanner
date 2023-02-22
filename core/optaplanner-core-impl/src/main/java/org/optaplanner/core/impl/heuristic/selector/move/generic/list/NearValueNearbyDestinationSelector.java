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
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.FromSolutionPropertyValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.util.MemoizingSupply;

// TODO extends abstract destination selector!
public final class NearValueNearbyDestinationSelector<Solution_> extends AbstractSelector<Solution_>
        implements DestinationSelector<Solution_> {

    private final EntitySelector<Solution_> childEntitySelector;
    private final EntityIndependentValueSelector<Solution_> childValueSelector;
    private final MimicReplayingValueSelector<Solution_> replayingOriginValueSelector;
    private final NearbyDistanceMeter<?, ?> nearbyDistanceMeter;
    private final NearbyRandom nearbyRandom;
    private final boolean randomSelection;
    private final ListNearbyDistanceMatrixDemand<Solution_, ?, ?> nearbyDistanceMatrixDemand;

    private MemoizingSupply<NearbyDistanceMatrix<Object, Object>> nearbyDistanceMatrixSupply = null;
    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    public NearValueNearbyDestinationSelector(
            EntitySelector<Solution_> childEntitySelector,
            EntityIndependentValueSelector<Solution_> childValueSelector,
            EntityIndependentValueSelector<Solution_> originValueSelector,
            NearbyDistanceMeter<?, ?> nearbyDistanceMeter,
            NearbyRandom nearbyRandom, boolean randomSelection) {
        this.childEntitySelector = childEntitySelector;
        this.childValueSelector = childValueSelector;
        if (!(originValueSelector instanceof MimicReplayingValueSelector)) {
            // In order to select a nearby destination, we must first have something to be near by.
            throw new IllegalStateException("Impossible state: Nearby destination selector (" + this +
                    ") did not receive a replaying value selector (" + originValueSelector + ").");
        }
        this.replayingOriginValueSelector = (MimicReplayingValueSelector<Solution_>) originValueSelector;
        this.nearbyDistanceMeter = nearbyDistanceMeter;
        this.nearbyRandom = nearbyRandom;
        this.randomSelection = randomSelection;
        if (randomSelection && nearbyRandom == null) {
            throw new IllegalArgumentException("The valueSelector (" + this
                    + ") with randomSelection (" + randomSelection + ") has no nearbyRandom (" + nearbyRandom + ").");
        }
        this.nearbyDistanceMatrixDemand =
                new ListNearbyDistanceMatrixDemand<>(
                        nearbyDistanceMeter,
                        ((MimicReplayingValueSelector<Solution_>) originValueSelector),
                        childEntitySelector,
                        ((FromSolutionPropertyValueSelector<Solution_>) childValueSelector),
                        this::computeDestinationSize);
        phaseLifecycleSupport.addEventListener(childEntitySelector);
        phaseLifecycleSupport.addEventListener(originValueSelector);
        phaseLifecycleSupport.addEventListener(childValueSelector);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        ListVariableDescriptor<Solution_> listVariableDescriptor =
                (ListVariableDescriptor<Solution_>) childValueSelector.getVariableDescriptor();
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
        long entitySelectorSize = childEntitySelector.getSize();
        if (entitySelectorSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The childEntitySelector (" + childEntitySelector
                    + ") has size (" + entitySelectorSize + ") which is higher than Integer.MAX_VALUE.");
        }
        long valueSelectorSize = childValueSelector.getSize();
        if (valueSelectorSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The childValueSelector (" + childValueSelector
                    + ") has size (" + valueSelectorSize + ") which is higher than Integer.MAX_VALUE.");
        }

        long childSize = entitySelectorSize + valueSelectorSize;
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The sum of the childEntitySelector (" + entitySelectorSize
                    + ") and the childValueSelector (" + valueSelectorSize
                    + ") sizes is higher than Integer.MAX_VALUE.");
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
        return childEntitySelector.isCountable() && childValueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || !isCountable();
    }

    @Override
    public long getSize() {
        return childEntitySelector.getSize() + childValueSelector.getSize();
    }

    @Override
    public Iterator<ElementRef> iterator() {
        Iterator<Object> replayingOriginValueIterator = replayingOriginValueSelector.iterator();
        if (!randomSelection) {
            return new OriginalValueNearbyDestinationIterator(replayingOriginValueIterator, childValueSelector.getSize());
        } else {
            return new RandomValueNearbyDestinationIterator(replayingOriginValueIterator, childValueSelector.getSize());
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
            Object next = nearbyDistanceMatrixSupply.read().getDestination(origin, nextNearbyIndex);
            nextNearbyIndex++;

            if (childEntitySelector.getEntityDescriptor().matchesEntity(next)) {
                return ElementRef.of(next, 0);
            }

            return ElementRef.of(
                    inverseVariableSupply.getInverseSingleton(next),
                    indexVariableSupply.getIndex(next) + 1);
        }

    }

    private final class RandomValueNearbyDestinationIterator extends SelectionIterator<ElementRef> {

        private final Iterator<Object> replayingOriginValueIterator;
        private final int nearbySize;

        public RandomValueNearbyDestinationIterator(Iterator<Object> replayingOriginValueIterator, long childSize) {
            this.replayingOriginValueIterator = replayingOriginValueIterator;
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The valueSelector (" + this
                        + ") has an entitySize (" + childSize
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
             * The originValue iterator is guaranteed to be a replaying iterator.
             * Therefore next() will point to whatever that the related recording iterator was pointing to at the time
             * when its next() was called.
             * As a result, originValue here will be constant unless next() on the original recording iterator is called
             * first.
             */
            Object originValue = replayingOriginValueIterator.next();
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            Object destinationAnchor = nearbyDistanceMatrixSupply.read().getDestination(originValue, nearbyIndex);

            if (childEntitySelector.getEntityDescriptor().matchesEntity(destinationAnchor)) {
                return ElementRef.of(destinationAnchor, 0);
            }

            return ElementRef.of(
                    inverseVariableSupply.getInverseSingleton(destinationAnchor),
                    indexVariableSupply.getIndex(destinationAnchor) + 1);
        }

    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        NearValueNearbyDestinationSelector<?> that = (NearValueNearbyDestinationSelector<?>) other;
        return randomSelection == that.randomSelection
                && Objects.equals(childEntitySelector, that.childEntitySelector)
                && Objects.equals(childValueSelector, that.childValueSelector)
                && Objects.equals(replayingOriginValueSelector, that.replayingOriginValueSelector)
                && Objects.equals(nearbyDistanceMeter, that.nearbyDistanceMeter)
                && Objects.equals(nearbyRandom, that.nearbyRandom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childEntitySelector, childValueSelector, replayingOriginValueSelector, nearbyDistanceMeter,
                nearbyRandom, randomSelection);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + replayingOriginValueSelector + ", " + childEntitySelector + ", "
                + childValueSelector + ")";
    }
}
