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

import org.acme.facilitylocation.domain.Consumer;
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
                    Consumer.class);

    @Test
    void penalizes_capacity_exceeded_by_a_single_consumer() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 20);
        Consumer consumer = new Consumer(0, location, 100);
        consumer.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer, facility)
                .penalizesBy(80);
    }

    @Test
    void no_penalty_when_demand_less_than_capacity() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 100);
        Consumer consumer1 = new Consumer(0, location, 1);
        Consumer consumer2 = new Consumer(0, location, 2);
        Consumer consumer3 = new Consumer(0, location, 3);
        consumer1.setFacility(facility);
        consumer2.setFacility(facility);
        consumer3.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer1, consumer2, consumer3, facility)
                .penalizesBy(0);
    }

    @Test
    void no_penalty_when_consumer_not_assigned() {
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, 0, 1);
        Consumer consumer = new Consumer(0, location, 100);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer, facility)
                .penalizesBy(0);
    }

    @Test
    void should_penalize_setup_cost() {
        long setupCost = 123;
        Location location = new Location(1, 1);
        Facility facility = new Facility(0, location, setupCost, 100);
        Consumer consumer = new Consumer(0, location, 1);
        consumer.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::setupCost)
                .given(facility, consumer)
                .penalizesBy(setupCost);
    }

    @Test
    void should_penalize_distance_to_facility() {
        Location facilityLocation = new Location(0, 0);
        Location consumer1Location = new Location(10, 0);
        Location consumer2Location = new Location(0, 20);

        Facility facility = new Facility(0, facilityLocation, 0, 100);
        Consumer consumer1 = new Consumer(0, consumer1Location, 1);
        Consumer consumer2 = new Consumer(0, consumer2Location, 1);

        consumer1.setFacility(facility);
        consumer2.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::distanceToFacility)
                .given(facility, consumer1, consumer2)
                .penalizesBy(30 * 111_000);
    }
}
