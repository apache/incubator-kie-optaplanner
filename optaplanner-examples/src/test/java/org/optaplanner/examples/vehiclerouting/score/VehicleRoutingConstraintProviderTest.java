/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.vehiclerouting.score;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Depot;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.location.AirLocation;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class VehicleRoutingConstraintProviderTest {

    private final ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutingSolution> constraintVerifier =
            ConstraintVerifier.build(new VehicleRoutingConstraintProvider(), VehicleRoutingSolution.class, Vehicle.class);

    private final Location location1 = new AirLocation(1L, 0.0, 0.0);
    private final Location location2 = new AirLocation(2L, 0.0, 4.0);
    private final Location location3 = new AirLocation(3L, 3.0, 0.0);

    private static void route(Vehicle vehicle, Customer... customers) {
        for (int i = 0; i < customers.length; i++) {
            customers[i].setIndex(i);
            customers[i].setVehicle(vehicle);
        }
        vehicle.setCustomers(Arrays.asList(customers));
    }

    @Test
    public void vehicleCapacityUnpenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        route(vehicleA, customer1);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1)
                .penalizesBy(0);
    }

    @Test
    public void vehicleCapacityPenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        Customer customer2 = new Customer(3L, location3, 40);
        route(vehicleA, customer1, customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(20);
    }

    @Test
    public void vehicleRouteDistance() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        Customer customer2 = new Customer(3L, location3, 40);

        route(vehicleA, customer1, customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleRouteDistance)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(12000L);
    }

}
