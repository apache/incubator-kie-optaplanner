package org.optaplanner.examples.vehiclerouting.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.CustomerOrVehicle;

public class CustomerNearbyDistanceMeter implements NearbyDistanceMeter<Customer, CustomerOrVehicle> {

    @Override
    public double getNearbyDistance(Customer origin, CustomerOrVehicle destination) {
        long distance = origin.getLocation().getDistanceTo(destination.getLocation());
        // If arriving early also inflicts a cost (more than just not using the vehicle more), such as the driver's wage, use this:
        //        if (origin instanceof TimeWindowedCustomer && destination instanceof TimeWindowedCustomer) {
        //            distance += ((TimeWindowedCustomer) origin).getTimeWindowGapTo((TimeWindowedCustomer) destination);
        //        }
        return distance;
    }

}
