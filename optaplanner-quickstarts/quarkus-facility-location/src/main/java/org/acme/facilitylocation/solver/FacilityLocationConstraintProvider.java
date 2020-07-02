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

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumLong;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                facilityCapacity(constraintFactory),
                setupCost(constraintFactory),
                distanceFromFacility(constraintFactory)
        };
    }

    public Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Consumer.class)
                .groupBy(Consumer::getFacility, sumLong(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalizeLong(
                        FacilityLocationConstraintConfiguration.FACILITY_CAPACITY,
                        HardSoftLongScore.ONE_HARD,
                        (facility, demand) -> demand - facility.getCapacity());
    }

    public Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Consumer.class)
                .groupBy(Consumer::getFacility)
                .penalizeConfigurableLong(
                        FacilityLocationConstraintConfiguration.FACILITY_SETUP_COST,
                        Facility::getSetupCost);
    }

    public Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Consumer.class)
                .filter(Consumer::isAssigned)
                .penalizeLong(
                        FacilityLocationConstraintConfiguration.DISTANCE_FROM_FACILITY,
                        HardSoftLongScore.ONE_SOFT,
                        Consumer::distanceFromFacility);
    }
}
