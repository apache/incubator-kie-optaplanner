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
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                facilityCapacity(constraintFactory),
                setupCost(constraintFactory)
        };
    }

    Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DemandPoint.class)
                .groupBy(DemandPoint::getFacility, ConstraintCollectors.sumLong(DemandPoint::getDemand))
                .filter((facility, demand) -> demand > facility.capacity)
                .penalizeLong(
                        "facility capacity",
                        HardSoftLongScore.ONE_HARD,
                        (facility, demand) -> demand - facility.capacity);
    }

    Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DemandPoint.class)
                .groupBy(DemandPoint::getFacility)
                .penalizeLong(
                        "facility setup cost",
                        HardSoftLongScore.ONE_SOFT,
                        facility -> facility.setupCost);
    }
}
