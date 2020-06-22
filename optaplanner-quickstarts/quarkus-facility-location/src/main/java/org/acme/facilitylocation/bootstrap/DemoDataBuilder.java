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

package org.acme.facilitylocation.bootstrap;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.facilitylocation.domain.DemandPoint;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;

public class DemoDataBuilder {

    private static final AtomicLong sequence = new AtomicLong();

    private long capacity;
    private long demand;
    private int facilityCount;
    private int demandPointCount;
    private long averageSetupCost;
    private long setupCostStandardDeviation;
    private Location southWestCorner;
    private Location northEastCorner;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setCapacity(long capacity) {
        this.capacity = capacity;
        return this;
    }

    public DemoDataBuilder setDemand(long demand) {
        this.demand = demand;
        return this;
    }

    public DemoDataBuilder setFacilityCount(int facilityCount) {
        this.facilityCount = facilityCount;
        return this;
    }

    public DemoDataBuilder setDemandPointCount(int demandPointCount) {
        this.demandPointCount = demandPointCount;
        return this;
    }

    public DemoDataBuilder setAverageSetupCost(long averageSetupCost) {
        this.averageSetupCost = averageSetupCost;
        return this;
    }

    public DemoDataBuilder setSetupCostStandardDeviation(long setupCostStandardDeviation) {
        this.setupCostStandardDeviation = setupCostStandardDeviation;
        return this;
    }

    public DemoDataBuilder setSouthWestCorner(Location southWestCorner) {
        this.southWestCorner = southWestCorner;
        return this;
    }

    public DemoDataBuilder setNorthEastCorner(Location northEastCorner) {
        this.northEastCorner = northEastCorner;
        return this;
    }

    public FacilityLocationProblem build() {
        if (demand < 1) {
            throw new IllegalStateException("Demand (" + demand + ") must be greater than zero.");
        }
        if (capacity < 1) {
            throw new IllegalStateException("Capacity (" + capacity + ") must be greater than zero.");
        }
        if (facilityCount < 1) {
            throw new IllegalStateException("Number of facilities (" + facilityCount + ") must be greater than zero.");
        }
        if (demandPointCount < 1) {
            throw new IllegalStateException("Number of demand points (" + demandPointCount + ") must be greater than zero.");
        }
        if (demand > capacity) {
            throw new IllegalStateException("Overconstrained problem not supported. The total capacity ("
                    + capacity + ") must be greater than or equal to the total demand (" + demand + ").");
        }
        // TODO SW<NE

        Random random = new Random();
        PrimitiveIterator.OfDouble latitudes = random.doubles(southWestCorner.latitude, northEastCorner.latitude)
                .iterator();
        PrimitiveIterator.OfDouble longitudes = random.doubles(southWestCorner.longitude, northEastCorner.longitude)
                .iterator();
        Supplier<Location> locationSupplier = () -> new Location(latitudes.nextDouble(), longitudes.nextDouble());
        List<Facility> facilities = Stream.generate(locationSupplier)
                .map(location -> new Facility(
                        sequence.incrementAndGet(),
                        location,
                        averageSetupCost + (long) (setupCostStandardDeviation * random.nextGaussian()),
                        capacity / facilityCount))
                .limit(facilityCount)
                .collect(Collectors.toList());
        List<DemandPoint> demandPoints = Stream.generate(locationSupplier)
                .map(location -> new DemandPoint(
                        sequence.incrementAndGet(),
                        location,
                        demand / demandPointCount))
                .limit(demandPointCount)
                .collect(Collectors.toList());

        return new FacilityLocationProblem(facilities, demandPoints, southWestCorner, northEastCorner);
    }
}
