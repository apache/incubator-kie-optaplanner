/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.flightcrewscheduling.score;

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.overlapping;

import java.time.LocalDate;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.examples.flightcrewscheduling.domain.Employee;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightAssignment;
import org.optaplanner.examples.flightcrewscheduling.domain.Skill;

public class FlightCrewSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredSkill(constraintFactory),
                flightConflict(constraintFactory),
                transferBetweenTwoFlights(constraintFactory),
                employeeUnavailability(constraintFactory),
                firstAssignmentNotDepartingFromHome(constraintFactory),
                lastAssignmentNotArrivingAtHome(constraintFactory)
        };
    }

    private Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(flightAssignment -> {
                    Skill requiredSkill = flightAssignment.getRequiredSkill();
                    return !flightAssignment.getEmployee().hasSkill(requiredSkill);
                })
                .penalize(HardSoftLongScore.ofHard(100))
                .asConstraint("Required skill");
    }

    private Constraint flightConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(FlightAssignment.class, equal(FlightAssignment::getEmployee),
                overlapping(flightAssignment -> flightAssignment.getFlight().getDepartureUTCDateTime(),
                        flightAssignment -> flightAssignment.getFlight().getArrivalUTCDateTime()))
                .penalize(HardSoftLongScore.ofHard(10))
                .asConstraint("Flight conflict");
    }

    private Constraint transferBetweenTwoFlights(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> employee.countInvalidConnections() > 0)
                .penalizeLong(HardSoftLongScore.ofHard(1), Employee::countInvalidConnections)
                .asConstraint("Transfer between two flights");
    }

    private Constraint employeeUnavailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(flightAssignment -> {
                    LocalDate departureUTCDate = flightAssignment.getFlight().getDepartureUTCDate();
                    return !flightAssignment.getEmployee().isAvailable(departureUTCDate);
                })
                .penalize(HardSoftLongScore.ofHard(10))
                .asConstraint("Employee unavailable");
    }

    private Constraint firstAssignmentNotDepartingFromHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> !employee.isFirstAssignmentDepartingFromHome())
                .penalize(HardSoftLongScore.ofSoft(1_000_000))
                .asConstraint("First assignment not departing from home");
    }

    private Constraint lastAssignmentNotArrivingAtHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .filter(employee -> !employee.isLastAssignmentArrivingAtHome())
                .penalize(HardSoftLongScore.ofSoft(1_000_000))
                .asConstraint("Last assignment not arriving at home");
    }

}
