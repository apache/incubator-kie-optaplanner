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

package org.acme.facilitylocation.solver;

import org.acme.facilitylocation.domain.DemandPoint;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class FacilityLocationConstraintProviderTest {

    private final ConstraintVerifier<FacilityLocationConstraintProvider, FacilityLocationProblem> constraintVerifier =
            ConstraintVerifier.build(
                    new FacilityLocationConstraintProvider(),
                    FacilityLocationProblem.class,
                    DemandPoint.class);

    @Test
    void penalizes_capacity_exceeded_by_a_single_demand_point() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 20);
        DemandPoint demandPoint = new DemandPoint(0, location, 100);
        demandPoint.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(demandPoint, facility)
                .penalizesBy(80);
    }

    @Test
    void no_penalty_when_demand_less_than_capacity() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 100);
        DemandPoint dp1 = new DemandPoint(0, location, 1);
        DemandPoint dp2 = new DemandPoint(0, location, 2);
        DemandPoint dp3 = new DemandPoint(0, location, 3);
        dp1.setFacility(facility);
        dp2.setFacility(facility);
        dp3.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(dp1, dp2, dp3, facility)
                .penalizesBy(0);
    }

    @Test
    void no_penalty_when_demand_point_not_assigned() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 1);
        DemandPoint demandPoint = new DemandPoint(0, location, 100);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(demandPoint, facility)
                .penalizesBy(0);
    }

    @Test
    void should_penalize_setup_cost() {
        long setupCost = 123;
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, setupCost, 100);
        DemandPoint demandPoint = new DemandPoint(0, location, 1);
        demandPoint.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::setupCost)
                .given(facility, demandPoint)
                .penalizesBy(setupCost);
    }
}
