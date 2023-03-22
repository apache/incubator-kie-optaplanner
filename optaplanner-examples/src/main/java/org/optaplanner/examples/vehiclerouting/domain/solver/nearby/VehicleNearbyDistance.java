package org.optaplanner.examples.vehiclerouting.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;

public class VehicleNearbyDistance implements NearbyDistanceMeter<Vehicle, Vehicle> {

    @Override
    public double getNearbyDistance(Vehicle origin, Vehicle destination) {
        return origin.getLocation().getDistanceTo(destination.getLocation());
    }

}
