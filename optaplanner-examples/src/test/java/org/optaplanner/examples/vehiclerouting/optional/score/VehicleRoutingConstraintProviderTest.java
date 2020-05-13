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

package org.optaplanner.examples.vehiclerouting.optional.score;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Depot;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.location.AirLocation;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedDepot;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class VehicleRoutingConstraintProviderTest {

    private final ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutingSolution> constraintVerifier =
            ConstraintVerifier.build(new VehicleRoutingConstraintProvider(), VehicleRoutingSolution.class, Standstill.class,
                    Customer.class, TimeWindowedCustomer.class);

    private final Location location1 = new AirLocation(1L, 0.0, 0.0);
    private final Location location2 = new AirLocation(2L, 0.0, 4.0);
    private final Location location3 = new AirLocation(3L, 3.0, 0.0);

    @Test
    public void vehicleCapacityUnpenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1)
                .penalizesBy(0);
    }

    @Test
    public void vehicleCapacityPenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(20);
    }

    @Test
    public void distanceToPreviousStandstill() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::distanceToPreviousStandstill)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(9000L);
    }

    @Test
    public void distanceFromLastCustomerToDepot() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::distanceFromLastCustomerToDepot)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(3000L);
    }

    @Test
    public void arrivalAfterDueTime() {
        Vehicle vehicleA = new Vehicle(1L, 100, new TimeWindowedDepot(1L, location1, 8_00_00L, 18_00_00L));
        TimeWindowedCustomer customer1 = new TimeWindowedCustomer(2L, location2, 1, 8_00_00L, 18_00_00L, 1_00_00L);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        customer1.setArrivalTime(8_00_00L + 4000L);
        TimeWindowedCustomer customer2 = new TimeWindowedCustomer(3L, location3, 40, 8_00_00L, 9_00_00L, 1_00_00L);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);
        customer2.setArrivalTime(8_00_00L + 4000L + 1_00_00L + 5000L);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::arrivalAfterDueTime)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(90_00L);
    }

}
