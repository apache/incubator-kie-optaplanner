package org.optaplanner.examples.vehiclerouting.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;

public class CustomerNearbyDistance implements NearbyDistanceMeter<Customer, Customer> {

    @Override
    public double getNearbyDistance(Customer origin, Customer destination) {
        return origin.getLocation().getDistanceTo(destination.getLocation());
    }

}
