package org.optaplanner.core.impl.heuristic.selector.common.nearby;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.Selector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

/**
 * Demands a distance matrix where the origins are planning entities and nearby destinations are either entities or values.
 * <p>
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by
 * {@link NearbyDistanceMatrixDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same supply instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_> planning entities
 * @param <Destination_> planning entities XOR planning values
 */
public final class NearbyDistanceMatrixDemand<Solution_, Origin_, Destination_>
        extends AbstractNearbyDistanceMatrixDemand<Origin_, Destination_, Selector<Solution_>, EntitySelector<Solution_>> {

    private final ToIntFunction<Origin_> destinationSizeFunction;

    public NearbyDistanceMatrixDemand(NearbyDistanceMeter<Origin_, Destination_> meter, NearbyRandom random,
            Selector<Solution_> childSelector, EntitySelector<Solution_> replayingOriginEntitySelector,
            ToIntFunction<Origin_> destinationSizeFunction) {
        super(meter, random, childSelector, replayingOriginEntitySelector);
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    protected NearbyDistanceMatrix<Origin_, Destination_> supplyNearbyDistanceMatrix() {
        if (childSelector instanceof EntitySelector) {
            final long childSize = ((EntitySelector<Solution_>) childSelector).getSize();
            if (childSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("The childEntitySelector (" + childSelector
                        + ") has an entitySize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
        }
        long originSize = replayingSelector.getSize();
        if (originSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The originEntitySelector (" + replayingSelector
                    + ") has an entitySize (" + originSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        // Destinations: entities or values extracted either from an entity selector or a value selector.
        Function<Origin_, Iterator<Destination_>> destinationIteratorProvider = childSelector instanceof EntitySelector
                ? origin -> (Iterator<Destination_>) ((EntitySelector<Solution_>) childSelector).endingIterator()
                : origin -> (Iterator<Destination_>) ((ValueSelector<Solution_>) childSelector).endingIterator(origin);
        NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix =
                new NearbyDistanceMatrix<>(meter, (int) originSize, destinationIteratorProvider, destinationSizeFunction);
        // Origins: entities extracted from an entity selector.
        replayingSelector.endingIterator()
                .forEachRemaining(origin -> nearbyDistanceMatrix.addAllDestinations((Origin_) origin));
        return nearbyDistanceMatrix;
    }

}
