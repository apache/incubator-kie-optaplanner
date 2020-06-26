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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void should_build_data() {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(1000)
                .setDemand(900)
                .setAverageSetupCost(1000).setSetupCostStandardDeviation(200)
                .setFacilityCount(10)
                .setDemandPointCount(150)
                .setSouthWestCorner(new Location(-10, -10))
                .setNorthEastCorner(new Location(20, 20))
                .build();

        assertEquals(10, problem.getFacilities().size());
        // Show toString().
        problem.getFacilities().forEach(System.out::println);
        problem.getFacilities().forEach(facility -> assertEquals(100, facility.getCapacity()));

        assertEquals(150, problem.getDemandPoints().size());
        // Show toString().
        problem.getDemandPoints().stream().limit(10).forEach(System.out::println);
        problem.getDemandPoints().forEach(demandPoint -> assertEquals(6, demandPoint.getDemand()));
    }

    @Test
    void correct_builder_builds_ok() {
        assertNotNull(correctBuilder().build());
    }

    @Test
    void capacity_greater_than_demand() {
        DemoDataBuilder builder = correctBuilder().setDemand(Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void capacity_greater_than_zero() {
        DemoDataBuilder builder = correctBuilder().setCapacity(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setCapacity(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void demand_greater_than_zero() {
        DemoDataBuilder builder = correctBuilder().setDemand(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setDemand(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void facility_count_greater_than_zero() {
        DemoDataBuilder builder = correctBuilder().setFacilityCount(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setFacilityCount(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void demand_point_count_greater_than_zero() {
        DemoDataBuilder builder = correctBuilder().setDemandPointCount(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setDemandPointCount(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setSouthWestCorner(new Location(-1, -1))
                .setNorthEastCorner(new Location(1, 1))
                .setCapacity(20)
                .setDemand(10)
                .setDemandPointCount(1)
                .setFacilityCount(1)
                .setAverageSetupCost(100)
                .setSetupCostStandardDeviation(1);
    }
}
