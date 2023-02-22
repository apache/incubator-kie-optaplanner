package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.FromSolutionPropertyValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;
import org.optaplanner.core.impl.solver.ClassInstanceCache;
import org.optaplanner.core.impl.util.MemoizingSupply;

/**
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 *
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by
 * {@link ListNearbyDistanceMatrixDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same supply instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_>
 * @param <Destination_>
 */
public final class ListNearbyDistanceMatrixDemand<Solution_, Origin_, Destination_>
        implements Demand<MemoizingSupply<NearbyDistanceMatrix<Origin_, Destination_>>> {

    private final NearbyDistanceMeter<Origin_, Destination_> meter;
    private final MimicReplayingValueSelector<Solution_> replayingOriginValueSelector;
    private final EntitySelector<Solution_> childEntitySelector;
    private final FromSolutionPropertyValueSelector<Solution_> childValueSelector;
    private final ToIntFunction<Origin_> destinationSizeFunction;

    public ListNearbyDistanceMatrixDemand(
            NearbyDistanceMeter<Origin_, Destination_> meter,
            MimicReplayingValueSelector<Solution_> replayingOriginValueSelector,
            EntitySelector<Solution_> childEntitySelector,
            FromSolutionPropertyValueSelector<Solution_> childValueSelector,
            ToIntFunction<Origin_> destinationSizeFunction) {
        this.meter = meter;
        this.replayingOriginValueSelector = replayingOriginValueSelector;
        this.childEntitySelector = childEntitySelector;
        this.childValueSelector = childValueSelector;
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    public MemoizingSupply<NearbyDistanceMatrix<Origin_, Destination_>> createExternalizedSupply(SupplyManager supplyManager) {
        Supplier<NearbyDistanceMatrix<Origin_, Destination_>> supplier = () -> {
            final long childSize = childEntitySelector.getSize() + childValueSelector.getSize();
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The childSize (" + childSize
                        + ") which is the sum of the childEntitySelector (" + childEntitySelector
                        + ") and the childValueSelector (" + childValueSelector
                        + ") sizes is higher than Integer.MAX_VALUE.");
            }

            long originSize = replayingOriginValueSelector.getSize();
            if (originSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The originEntitySelector (" + replayingOriginValueSelector
                        + ") has an entitySize (" + originSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            // Destinations in the "matrix" need to be entities and values (not ElementRefs!) because that's what
            // the NearbyDistanceMeter is able to measure.
            Function<Origin_, Iterator<Destination_>> destinationIteratorProvider =
                    origin -> (Iterator<Destination_>) Stream.concat(
                            StreamSupport.stream(Spliterators.spliterator(childEntitySelector.endingIterator(),
                                    childEntitySelector.getSize(), 0), false),
                            StreamSupport.stream(Spliterators.spliterator(childValueSelector.endingIterator(),
                                    childValueSelector.getSize(), 0), false))
                            .iterator();
            NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix =
                    new NearbyDistanceMatrix<>(meter, (int) originSize, destinationIteratorProvider, destinationSizeFunction);
            // Replaying selector's ending iterator will use the recording selector's ending iterator. Since list variables
            // use entity independent value selectors, we can pass null here.
            replayingOriginValueSelector.endingIterator(null)
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
        ListNearbyDistanceMatrixDemand<?, ?, ?> that = (ListNearbyDistanceMatrixDemand<?, ?, ?>) o;
        return Objects.equals(meter, that.meter)
                && Objects.equals(replayingOriginValueSelector, that.replayingOriginValueSelector)
                && Objects.equals(childEntitySelector, that.childEntitySelector)
                && Objects.equals(childValueSelector, that.childValueSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meter, replayingOriginValueSelector, childEntitySelector, childValueSelector);
    }
}
