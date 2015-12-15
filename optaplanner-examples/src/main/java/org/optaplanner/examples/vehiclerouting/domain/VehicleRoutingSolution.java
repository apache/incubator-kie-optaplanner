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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.buildin.hardsoftlong.HardSoftLongScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedVehicleRoutingSolution;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@PlanningSolution
@XStreamAlias("VrpVehicleRoutingSolution")
@XStreamInclude({
        TimeWindowedVehicleRoutingSolution.class
})
public class VehicleRoutingSolution extends AbstractPersistable implements Solution<HardSoftLongScore> {

    protected String name;
    protected DistanceType distanceType;
    protected String distanceUnitOfMeasurement;
    protected List<Location> locationList;
    protected List<Depot> depotList;
    protected List<Vehicle> vehicleList;

    protected List<Customer> customerList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftLongScoreDefinition.class})
    protected HardSoftLongScore score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DistanceType getDistanceType() {
        return distanceType;
    }

    public void setDistanceType(DistanceType distanceType) {
        this.distanceType = distanceType;
    }

    public String getDistanceUnitOfMeasurement() {
        return distanceUnitOfMeasurement;
    }

    public void setDistanceUnitOfMeasurement(String distanceUnitOfMeasurement) {
        this.distanceUnitOfMeasurement = distanceUnitOfMeasurement;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    public List<Depot> getDepotList() {
        return depotList;
    }

    public void setDepotList(List<Depot> depotList) {
        this.depotList = depotList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "vehicleRange")
    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "customerRange")
    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(locationList);
        facts.addAll(depotList);
        // Do not add the planning entities (vehicleList, customerList) because that will be done automatically
        return facts;
    }

    public String getDistanceString(NumberFormat numberFormat) {
        if (score == null) {
            return null;
        }
        long distance = - score.getSoftScore();
        if (distanceUnitOfMeasurement == null) {
            return numberFormat.format(((double) distance) / 1000.0);
        }
        if (distanceUnitOfMeasurement.equals("sec")) { // TODO why are the values 1000 larger?
            long hours = distance / 3600000L;
            long minutes = distance % 3600000L / 60000L;
            long seconds = distance % 60000L / 1000L;
            long milliseconds = distance % 1000L;
            return hours + "h " + minutes + "m " + seconds + "s " + milliseconds + "ms";
        } else if (distanceUnitOfMeasurement.equals("km")) { // TODO why are the values 1000 larger?
            long km = distance / 1000L;
            long meter = distance % 1000L;
            return km + "km " + meter + "m";
        } else if (distanceUnitOfMeasurement.equals("meter")) {
            long km = distance / 1000L;
            long meter = distance % 1000L;
            return km + "km " + meter + "m";
        } else {
            return numberFormat.format(((double) distance) / 1000.0) + " " + distanceUnitOfMeasurement;
        }
    }

}
