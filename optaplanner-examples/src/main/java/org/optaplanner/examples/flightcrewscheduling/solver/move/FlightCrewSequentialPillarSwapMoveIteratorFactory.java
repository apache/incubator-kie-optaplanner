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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.cheaptime.solver.move.CheapTimePillarSlideMove;
import org.optaplanner.examples.flightcrewscheduling.domain.Employee;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightAssignment;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightCrewSolution;

public class FlightCrewSequentialPillarSwapMoveIteratorFactory implements MoveIteratorFactory<FlightCrewSolution> {

    @Override
    public long getSize(ScoreDirector<FlightCrewSolution> scoreDirector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Move<FlightCrewSolution>> createOriginalMoveIterator(ScoreDirector<FlightCrewSolution> scoreDirector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomFlightCrewSequentialPillarSwapMoveIterator createRandomMoveIterator(
            ScoreDirector<FlightCrewSolution> scoreDirector, Random workingRandom) {
        FlightCrewSolution solution = scoreDirector.getWorkingSolution();
        return new RandomFlightCrewSequentialPillarSwapMoveIterator(solution.getEmployeeList(), workingRandom);
    }

    public static class RandomFlightCrewSequentialPillarSwapMoveIterator implements Iterator<FlightCrewSequentialPillarSwapMove> {

        private final List<Employee> employeeList;
        private final Random workingRandom;

        public RandomFlightCrewSequentialPillarSwapMoveIterator(List<Employee> employeeList, Random workingRandom) {
            this.employeeList = employeeList;
            this.workingRandom = workingRandom;
        }

        @Override
        public boolean hasNext() {
            return employeeList.size() >= 2;
        }

        @Override
        public FlightCrewSequentialPillarSwapMove next() {
            // TODO once we support overconstrained planning (nullable = true),
            // it should be possible to select leftEmployee as null and the flightAssignmentSet assigned to null
            int leftEmployeeIndex = workingRandom.nextInt(employeeList.size());
            int rightEmployeeIndex = workingRandom.nextInt(employeeList.size() - 1);
            if (rightEmployeeIndex >= leftEmployeeIndex) {
                rightEmployeeIndex++;
            }
            Employee leftEmployee = employeeList.get(leftEmployeeIndex);
            Employee rightEmployee = employeeList.get(rightEmployeeIndex);
            List<FlightAssignment> leftFlightAssignmentList = selectSequentialSubPillar(leftEmployee.getFlightAssignmentSet(), 2);
            List<FlightAssignment> rightFlightAssignmentList = selectSequentialSubPillar(rightEmployee.getFlightAssignmentSet(), 0);
            return new FlightCrewSequentialPillarSwapMove(leftEmployee, leftFlightAssignmentList, rightEmployee, rightFlightAssignmentList);
        }

        private List<FlightAssignment> selectSequentialSubPillar(SortedSet<FlightAssignment> flightAssignmentSet, int minimumSize) {
            List<FlightAssignment> assignmentList = new ArrayList<>(flightAssignmentSet);
            if (assignmentList.size() < minimumSize) {
                minimumSize = assignmentList.size();
            }
            int fromAssignmentIndex = workingRandom.nextInt(assignmentList.size() - minimumSize + 1);
            int toAssignmentIndex = fromAssignmentIndex + minimumSize
                    + workingRandom.nextInt(assignmentList.size() - fromAssignmentIndex - minimumSize + 1);
            return assignmentList.subList(fromAssignmentIndex, toAssignmentIndex);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("The optional operation remove() is not supported.");
        }

    }

}
