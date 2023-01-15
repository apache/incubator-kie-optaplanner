package org.optaplanner.core.impl.heuristic.selector.common.nearby;

import org.optaplanner.core.impl.domain.variable.supply.Supply;

@FunctionalInterface
public interface NearbySupply<Origin_, Destination_> extends Supply {

    NearbyDistanceMatrix<Origin_, Destination_> getDistanceMatrix();

}
