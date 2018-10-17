/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.flightcrewscheduling.solver.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.flightcrewscheduling.domain.Employee;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightAssignment;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightCrewSolution;

public class FlightCrewSequentialPillarSwapMove extends AbstractMove<FlightCrewSolution> {

    private final Employee leftEmployee;
    private final List<FlightAssignment> leftFlightAssignmentList;
    private final Employee rightEmployee;
    private final List<FlightAssignment> rightFlightAssignmentList;

    public FlightCrewSequentialPillarSwapMove(Employee leftEmployee, List<FlightAssignment> leftFlightAssignmentList,
            Employee rightEmployee, List<FlightAssignment> rightFlightAssignmentList) {
        this.leftEmployee = leftEmployee;
        this.leftFlightAssignmentList = leftFlightAssignmentList;
        this.rightEmployee = rightEmployee;
        this.rightFlightAssignmentList = rightFlightAssignmentList;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<FlightCrewSolution> scoreDirector) {
        return true;
    }

    @Override
    public FlightCrewSequentialPillarSwapMove createUndoMove(ScoreDirector<FlightCrewSolution> scoreDirector) {
        return new FlightCrewSequentialPillarSwapMove(
                rightEmployee, leftFlightAssignmentList, leftEmployee, rightFlightAssignmentList);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<FlightCrewSolution> scoreDirector) {
        for (FlightAssignment flightAssignment : leftFlightAssignmentList) {
            scoreDirector.beforeVariableChanged(flightAssignment, "employee");
            flightAssignment.setEmployee(rightEmployee);
            scoreDirector.afterVariableChanged(flightAssignment, "employee");
        }
        for (FlightAssignment flightAssignment : rightFlightAssignmentList) {
            scoreDirector.beforeVariableChanged(flightAssignment, "employee");
            flightAssignment.setEmployee(leftEmployee);
            scoreDirector.afterVariableChanged(flightAssignment, "employee");
        }
    }

    @Override
    public FlightCrewSequentialPillarSwapMove rebase(ScoreDirector<FlightCrewSolution> destinationScoreDirector) {
        return new FlightCrewSequentialPillarSwapMove(
                leftEmployee, rebaseList(leftFlightAssignmentList, destinationScoreDirector),
                rightEmployee, rebaseList(rightFlightAssignmentList, destinationScoreDirector));
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        List<Object> entities = new ArrayList<>(
                leftFlightAssignmentList.size() + rightFlightAssignmentList.size());
        entities.addAll(leftFlightAssignmentList);
        entities.addAll(rightFlightAssignmentList);
        return entities;
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Arrays.asList(leftEmployee, rightEmployee);
    }

    @Override
    public String toString() {
        return leftEmployee + " {" + leftFlightAssignmentList
                + "} <-> " + rightEmployee + " {" + rightFlightAssignmentList + "}";
    }

}
