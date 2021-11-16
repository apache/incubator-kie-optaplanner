/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.vehiclerouting.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningCollectionVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.solver.DepotAngleCustomerDifficultyWeightFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("VrpVehicle")
@PlanningEntity
public class Vehicle extends AbstractPersistable {

    protected int capacity;
    protected Depot depot;

    @PlanningCollectionVariable(valueRangeProviderRefs = "customerRange",
            strengthWeightFactoryClass = DepotAngleCustomerDifficultyWeightFactory.class)
    private List<Customer> customers = new ArrayList<>();

    public Vehicle() {
    }

    public Vehicle(long id, int capacity, Depot depot) {
        super(id);
        this.capacity = capacity;
        this.depot = depot;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Location getLocation() {
        return depot.getLocation();
    }

    /**
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getDistanceTo(Location location) {
        return depot.getDistanceTo(location);
    }

    @Override
    public String toString() {
        Location location = getLocation();
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName() + "/" + super.toString();
    }

    public long getDistance() {
        if (customers.isEmpty()) {
            return 0;
        }
        int distance = 0;
        Location previous = depot.getLocation();
        for (Customer customer : customers) {
            distance += previous.getDistanceTo(customer.getLocation());
            previous = customer.getLocation();
        }
        return distance + previous.getDistanceTo(depot.getLocation());
    }
}
