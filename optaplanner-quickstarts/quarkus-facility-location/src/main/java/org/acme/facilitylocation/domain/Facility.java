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

package org.acme.facilitylocation.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

/**
 * This is a shadow planning entity, not a genuine planning entity, because it has a shadow variable (usage).
 */
@PlanningEntity
public class Facility {

    private long id;
    private Location location;
    private long setupCost;
    private long capacity;

    @CustomShadowVariable(variableListenerClass = UsageVariableListener.class,
            sources = @PlanningVariableReference(entityClass = DemandPoint.class, variableName = "facility"))
    private Long usage = 0L;

    public Facility() {
    }

    public Facility(long id, Location location, long setupCost, long capacity) {
        this.id = id;
        this.location = location;
        this.setupCost = setupCost;
        this.capacity = capacity;
    }

    void remove(DemandPoint demandPoint) {
        usage -= demandPoint.getDemand();
    }

    void add(DemandPoint demandPoint) {
        usage += demandPoint.getDemand();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getSetupCost() {
        return setupCost;
    }

    public void setSetupCost(long setupCost) {
        this.setupCost = setupCost;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getUsage() {
        return usage;
    }

    public void setUsage(long usage) {
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "Facility " + id +
                " capacity: " + capacity +
                " cost: $" + setupCost;
    }
}
