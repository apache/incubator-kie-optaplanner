package org.optaplanner.examples.cloudbalancing.domain.solver.nearby;

import static java.lang.Math.abs;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;

public class CloudProcessNearbyDistanceMeter implements NearbyDistanceMeter<CloudProcess, CloudProcess> {

    @Override
    public double getNearbyDistance(CloudProcess origin, CloudProcess destination) {
        return abs(origin.getRequiredMultiplicand() - destination.getRequiredMultiplicand());
    }

}
