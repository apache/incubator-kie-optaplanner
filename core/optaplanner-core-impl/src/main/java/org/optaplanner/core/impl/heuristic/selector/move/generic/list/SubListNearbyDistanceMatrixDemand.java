package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicReplayingSubListSelector;
import org.optaplanner.core.impl.solver.ClassInstanceCache;
import org.optaplanner.core.impl.util.MemoizingSupply;

/**
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 *
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by
 * {@link SubListNearbyDistanceMatrixDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same supply instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_>
 * @param <Destination_>
 */
public final class SubListNearbyDistanceMatrixDemand<Solution_, Origin_, Destination_>
        implements Demand<MemoizingSupply<NearbyDistanceMatrix<Origin_, Destination_>>> {

    private final NearbyDistanceMeter<Origin_, Destination_> meter;
    private final ElementDestinationSelector<Solution_> childDestinationSelector;
    private final MimicReplayingSubListSelector<Solution_> replayingOriginSubListSelector;
    private final ToIntFunction<Origin_> destinationSizeFunction;

    public SubListNearbyDistanceMatrixDemand(
            NearbyDistanceMeter<Origin_, Destination_> meter,
            ElementDestinationSelector<Solution_> childDestinationSelector,
            MimicReplayingSubListSelector<Solution_> replayingOriginSubListSelector,
            ToIntFunction<Origin_> destinationSizeFunction) {
        this.meter = meter;
        this.childDestinationSelector = childDestinationSelector;
        this.replayingOriginSubListSelector = replayingOriginSubListSelector;
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    public MemoizingSupply<NearbyDistanceMatrix<Origin_, Destination_>> createExternalizedSupply(SupplyManager supplyManager) {
        Supplier<NearbyDistanceMatrix<Origin_, Destination_>> supplier = () -> {
            final long childSize = childDestinationSelector.getSize();
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The childSize (" + childSize + ") is higher than Integer.MAX_VALUE.");
            }

            long originSize = replayingOriginSubListSelector.getSize();
            if (originSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The originSubListSelector (" + replayingOriginSubListSelector
                        + ") has a subListSize (" + originSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            // Destinations in the "matrix" need to be entities and values (not ElementRefs!) because that's what
            // the NearbyDistanceMeter is able to measure.
            Function<Origin_, Iterator<Destination_>> destinationIteratorProvider =
                    origin -> (Iterator<Destination_>) childDestinationSelector.endingIterator();
            NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix =
                    new NearbyDistanceMatrix<>(meter, (int) originSize, destinationIteratorProvider, destinationSizeFunction);
            replayingOriginSubListSelector.endingValueIterator()
                    .forEachRemaining(origin -> nearbyDistanceMatrix.addAllDestinations((Origin_) origin));
            return nearbyDistanceMatrix;
        };
        return new MemoizingSupply<>(supplier);
    }

    /**
     * Two instances of this class are consider equal if and only if:
     *
     * <ul>
     * <li>Their meter instances are equal.</li>
     * <li>Their child selectors are equal.</li>
     * <li>Their replaying origin entity selectors are equal.</li>
     * </ul>
     *
     * Otherwise as defined by {@link Object#equals(Object)}.
     *
     * @see ClassInstanceCache for how we ensure equality for meter instances in particular and selectors in general.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubListNearbyDistanceMatrixDemand<?, ?, ?> that = (SubListNearbyDistanceMatrixDemand<?, ?, ?>) o;
        return Objects.equals(meter, that.meter)
                && Objects.equals(childDestinationSelector, that.childDestinationSelector)
                && Objects.equals(replayingOriginSubListSelector, that.replayingOriginSubListSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meter, childDestinationSelector, replayingOriginSubListSelector);
    }
}
