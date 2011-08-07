/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.examples.nurserostering.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.phase.custom.CustomSolverPhaseCommand;
import org.drools.planner.core.score.DefaultHardAndSoftScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.director.SolutionDirector;
import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NurseRosteringStartingSolutionInitializer implements CustomSolverPhaseCommand {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public void changeWorkingSolution(SolutionDirector solutionDirector) {
        NurseRoster nurseRoster = (NurseRoster) solutionDirector.getWorkingSolution();
        initializeAssignmentList(solutionDirector, nurseRoster);
    }

    private void initializeAssignmentList(SolutionDirector solutionDirector,
            NurseRoster nurseRoster) {
        List<Employee> employeeList = nurseRoster.getEmployeeList();
        WorkingMemory workingMemory = solutionDirector.getWorkingMemory();

        List<Assignment> assignmentList = createAssignmentList(nurseRoster);
        for (Assignment assignment : assignmentList) {
            FactHandle assignmentHandle = null;
            Score bestScore = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE, Integer.MIN_VALUE);
            Employee bestEmployee = null;
            for (Employee employee : employeeList) {
                assignment.setEmployee(employee);
                if (assignmentHandle == null) {
                    assignmentHandle = workingMemory.insert(assignment);
                } else {
                    workingMemory.update(assignmentHandle, assignment);
                }
                Score score = solutionDirector.calculateScoreFromWorkingMemory();
                if (score.compareTo(bestScore) > 0) {
                    bestScore = score;
                    bestEmployee = employee;
                }
            }
            if (bestEmployee == null) {
                throw new IllegalStateException("The bestEmployee (" + bestEmployee + ") cannot be null.");
            }
            assignment.setEmployee(bestEmployee);
            workingMemory.update(assignmentHandle, assignment);
            logger.debug("    Assignment ({}) initialized for starting solution.", assignment);
        }

        Collections.sort(assignmentList, new PersistableIdComparator());
        nurseRoster.setAssignmentList(assignmentList);
    }

    public List<Assignment> createAssignmentList(NurseRoster nurseRoster) {
        List<Shift> shiftList = nurseRoster.getShiftList();
        List<ShiftDate> shiftDateList = nurseRoster.getShiftDateList();

        List<ShiftInitializationWeight> shiftInitializationWeightList
                = new ArrayList<ShiftInitializationWeight>(shiftList.size());
        for (Shift shift : shiftList) {
            shiftInitializationWeightList.add(new ShiftInitializationWeight(nurseRoster, shift));
        }
        Collections.sort(shiftInitializationWeightList);

        List<Assignment> assignmentList = new ArrayList<Assignment>(
                shiftDateList.size() * nurseRoster.getEmployeeList().size());
        int assignmentId = 0;
        for (ShiftInitializationWeight shiftInitializationWeight : shiftInitializationWeightList) {
            Shift shift = shiftInitializationWeight.getShift();
            for (int i = 0; i < shift.getRequiredEmployeeSize(); i++) {
                Assignment assignment = new Assignment();
                assignment.setId((long) assignmentId);
                assignment.setShift(shift);
                assignmentList.add(assignment);
                assignmentId++;
            }
        }
        return assignmentList;
    }

    private class ShiftInitializationWeight implements Comparable<ShiftInitializationWeight> {

        private Shift shift;

        private ShiftInitializationWeight(NurseRoster nurseRoster, Shift shift) {
            this.shift = shift;
        }

        public Shift getShift() {
            return shift;
        }

        public int compareTo(ShiftInitializationWeight other) {
            return new CompareToBuilder()
                    .append(shift.getShiftDate(), other.shift.getShiftDate()) // Ascending
                    .append(other.shift.getRequiredEmployeeSize(), shift.getRequiredEmployeeSize()) // Descending
                    .append(shift.getShiftType(), other.shift.getShiftType()) // Ascending
                    .toComparison();
        }

    }

}
