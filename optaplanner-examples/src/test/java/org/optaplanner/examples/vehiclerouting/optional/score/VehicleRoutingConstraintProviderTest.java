/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.vehiclerouting.optional.score;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Depot;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class VehicleRoutingConstraintProviderTest {

    private final ConstraintVerifier<VehicleRoutingSolution> constraintVerifier = ConstraintVerifier.build(
            VehicleRoutingSolution.class,
            Standstill.class,
            Customer.class
    );

    private static RoadLocation.DistanceMap zeroDistanceAll() {
        return location -> 0;
    }

    private RoadLocation testLocation(long id, RoadLocation.DistanceMap distanceMap) {
        RoadLocation roadLocation = new RoadLocation();
        roadLocation.setId(id);
        roadLocation.setTravelDistanceMap(distanceMap);
        return roadLocation;
    }

    private static void route(Vehicle vehicle, Customer... visits) {
        vehicle.setNextCustomer(visits[0]);

        Standstill previousStandstill = vehicle;

        for (Customer visit : visits) {
            visit.setVehicle(vehicle);
            visit.setPreviousStandstill(previousStandstill);
            previousStandstill.setNextCustomer(visit);
            previousStandstill = visit;
        }
    }

    private static VehicleRoutingSolution solution(Vehicle vehicle, Customer customer) {
        VehicleRoutingSolution solution = new VehicleRoutingSolution();
        solution.setCustomerList(Collections.singletonList(customer));
        solution.setDepotList(Collections.singletonList(vehicle.getDepot()));
        solution.setVehicleList(Collections.singletonList(vehicle));
        solution.setLocationList(Arrays.asList(vehicle.getDepot().getLocation(), customer.getLocation()));
        return solution;
    }

    @Test
    public void vehicleCapacityExceeded_1vehicle_1visit() {
        int demand = 100;
        int capacity = 5;

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        Depot depot = new Depot();
        depot.setId(1L);
        depot.setLocation(testLocation(1, zeroDistanceAll()));
        vehicle.setDepot(depot);

        // CAPACITY
        vehicle.setCapacity(capacity);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setLocation(testLocation(2, zeroDistanceAll()));

        // DEMAND
        customer.setDemand(demand);

        // CONNECT
        route(vehicle, customer);

        // SOLUTION - works OK
        constraintVerifier.verifyThat(new VehicleRoutingConstraintProvider())
                .given(solution(vehicle, customer))
                .scores(HardSoftLongScore.ofHard(-(demand - capacity)));

        // FACTS - should work with non-configurable penalties/rewards but it fails:
        /*
java.lang.AssertionError: Broken expectation.
    Constraint provider: class org.optaplanner.examples.vehiclerouting.optional.score.VehicleRoutingConstraintProvider
         Expected score: -95hard/0soft (class org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore)
           Actual score: 0hard/95soft (class org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore)
         */
        constraintVerifier.verifyThat(new VehicleRoutingConstraintProvider())
                .given(vehicle, customer)
                .scores(HardSoftLongScore.ofHard(-(demand - capacity)));
    }
}
