/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

/**
 * Demands a distance matrix where the origins are planning entities and nearby destinations are either entities or values.
 * <p>
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by
 * {@link ValueNearbyDistanceMatrixDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same supply instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_> planning entities
 * @param <Destination_> planning entities XOR planning values
 */
final class ValueNearbyDistanceMatrixDemand<Solution_, Origin_, Destination_>
        extends AbstractNearbyDistanceMatrixDemand<Origin_, Destination_, ValueSelector<Solution_>, EntitySelector<Solution_>> {

    private final ToIntFunction<Origin_> destinationSizeFunction;

    public ValueNearbyDistanceMatrixDemand(NearbyDistanceMeter<Origin_, Destination_> meter, NearbyRandom random,
            ValueSelector<Solution_> childSelector, EntitySelector<Solution_> replayingOriginEntitySelector,
            ToIntFunction<Origin_> destinationSizeFunction) {
        super(meter, random, childSelector, replayingOriginEntitySelector);
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    protected NearbyDistanceMatrix<Origin_, Destination_> supplyNearbyDistanceMatrix() {
        long originSize = replayingSelector.getSize();
        if (originSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The originEntitySelector (" + replayingSelector
                    + ") has an entitySize (" + originSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        // Destinations: values extracted a value selector.
        Function<Origin_, Iterator<Destination_>> destinationIteratorProvider =
                origin -> (Iterator<Destination_>) childSelector.endingIterator(origin);
        NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix =
                new NearbyDistanceMatrix<>(meter, (int) originSize, destinationIteratorProvider, destinationSizeFunction);
        // Origins: entities extracted from an entity selector.
        replayingSelector.endingIterator()
                .forEachRemaining(origin -> nearbyDistanceMatrix.addAllDestinations((Origin_) origin));
        return nearbyDistanceMatrix;
    }

}
