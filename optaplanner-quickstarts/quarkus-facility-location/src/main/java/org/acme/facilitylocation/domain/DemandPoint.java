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

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class DemandPoint {

    // Approximate Metric Equivalents for Degrees. At the equator for longitude and for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles) 0.1° = 11.1 km.
    protected static final double METERS_PER_DEGREE = 111_000;

    private long id;
    private Location location;
    private long demand;

    @PlanningVariable(valueRangeProviderRefs = "facilityRange")
    private Facility facility;

    public DemandPoint() {
    }

    public DemandPoint(long id, Location location, long demand) {
        this.id = id;
        this.location = location;
        this.demand = demand;
    }

    public boolean isAssigned() {
        return facility != null;
    }

    /**
     * Get distance to the facility.
     *
     * @return distance in meters
     */
    public long distanceToFacility() {
        if (facility == null) {
            throw new IllegalStateException("No facility is assigned.");
        }
        double latDiff = facility.location.latitude - this.location.latitude;
        double lngDiff = facility.location.longitude - this.location.longitude;
        return (long) ceil(sqrt(latDiff * latDiff + lngDiff * lngDiff) * METERS_PER_DEGREE);
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

    public long getDemand() {
        return demand;
    }

    public void setDemand(long demand) {
        this.demand = demand;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "Demand " + id + ": " + demand;
    }
}
