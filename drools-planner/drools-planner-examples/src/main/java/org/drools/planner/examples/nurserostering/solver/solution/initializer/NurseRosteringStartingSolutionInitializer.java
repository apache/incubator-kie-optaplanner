package org.drools.planner.examples.nurserostering.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.DefaultHardAndSoftScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    @Override
    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        NurseRoster schedule = (NurseRoster) localSearchSolverScope.getWorkingSolution();
        return schedule.isInitialized();
    }

    public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
        NurseRoster schedule = (NurseRoster) localSearchSolverScope.getWorkingSolution();
        initializeAssignmentList(localSearchSolverScope, schedule);
    }

    private void initializeAssignmentList(LocalSearchSolverScope localSearchSolverScope,
            NurseRoster nurseRoster) {
        List<Employee> employeeList = nurseRoster.getEmployeeList();
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();

        List<Assignment> assignmentList = createAssignmentList(nurseRoster);
        for (Assignment assignment : assignmentList) {
            FactHandle assignmentHandle = null;
            Score bestScore = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE, Integer.MIN_VALUE);
            Employee bestEmployee = null;
            for (Employee employee : employeeList) {
                if (assignmentHandle == null) {
                    assignment.setEmployee(employee);
                    assignmentHandle = workingMemory.insert(assignment);
                } else {
                    assignment.setEmployee(employee);
                    workingMemory.update(assignmentHandle, assignment);
                }
                Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                if (score.compareTo(bestScore) > 0) {
                    bestScore = score;
                    bestEmployee = employee;
                }
            }
            if (bestEmployee == null || bestEmployee == null) {
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
        Collections.sort(assignmentList);
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
