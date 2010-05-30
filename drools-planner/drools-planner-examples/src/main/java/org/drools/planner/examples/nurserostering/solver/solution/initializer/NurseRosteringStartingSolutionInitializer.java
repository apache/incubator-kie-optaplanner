package org.drools.planner.examples.nurserostering.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        List<Shift> shiftList = nurseRoster.getShiftList();
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();

        List<Assignment> assignmentList = createAssignmentList(nurseRoster);
        for (Assignment assignment : assignmentList) {
            FactHandle assignmentHandle = null;
            Score bestScore = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE, Integer.MIN_VALUE);
            Shift bestShift = null;
            for (Shift shift : shiftList) {
                if (assignmentHandle == null) {
                    assignment.setShift(shift);
                    assignmentHandle = workingMemory.insert(assignment);
                } else {
                    assignment.setShift(shift);
                    workingMemory.update(assignmentHandle, assignment);
                }
                Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                if (score.compareTo(bestScore) > 0) {
                    bestScore = score;
                    bestShift = shift;
                }
            }
            if (bestShift == null || bestShift == null) {
                throw new IllegalStateException("The bestShift (" + bestShift + ") cannot be null.");
            }
            assignment.setShift(bestShift);
            workingMemory.update(assignmentHandle, assignment);
            logger.debug("    Assignment ({}) initialized for starting solution.", assignment);
        }

        Collections.sort(assignmentList, new PersistableIdComparator());
        nurseRoster.setEmployeeAssignmentList(assignmentList);
    }

    public List<Assignment> createAssignmentList(NurseRoster nurseRoster) {
        List<Employee> employeeList = nurseRoster.getEmployeeList();

//        List<EmployeeInitializationWeight> employeeInitializationWeightList
//                = new ArrayList<EmployeeInitializationWeight>(employeeList.size());
//        for (Employee employee : employeeList) {
//            employeeInitializationWeightList.add(new EmployeeInitializationWeight(nurseRoster, employee));
//        }
//        Collections.sort(employeeInitializationWeightList);
//
        List<Assignment> assignmentList = new ArrayList<Assignment>(employeeList.size() * 5);
//        int employeeAssignmentId = 0;
//        for (EmployeeInitializationWeight employeeInitializationWeight : employeeInitializationWeightList) {
//            Employee employee = employeeInitializationWeight.getEmployee();
//            for (int i = 0; i < employee.getEmployeeAssignmentSize(); i++) {
//                Assignment employeeAssignment = new Assignment();
//                employeeAssignment.setId((long) employeeAssignmentId);
//                employeeAssignmentId++;
//                employeeAssignment.setEmployee(employee);
//                employeeAssignment.setEmployeeAssignmentIndexInEmployee(i);
//                assignmentList.add(employeeAssignment);
//            }
//        }

        // TODO tmp begin
        List<Shift> shiftList = nurseRoster.getShiftList();
        int employeeAssignmentId = 0;
        Random random = new Random(); // not seeded, tmp!
        for (Shift shift : shiftList) {
            for (int i = 0; i < shift.getRequiredEmployeeSize(); i++) {
                Assignment assignment = new Assignment();
                assignment.setId((long) employeeAssignmentId);
                employeeAssignmentId++;
                assignment.setShift(shift);
                int randomInt = random.nextInt(employeeList.size());
                assignment.setEmployee(employeeList.get(randomInt));
                assignmentList.add(assignment);
            }
        }
        // TODO tmp end

        return assignmentList;
    }

//    private class EmployeeInitializationWeight implements Comparable<EmployeeInitializationWeight> {
//
//        private Employee employee;
//        private int unavailableShiftConstraintCount;
//
//        private EmployeeInitializationWeight(NurseRoster nurseRoster, Employee employee) {
//            this.employee = employee;
//            unavailableShiftConstraintCount = 0;
//            // TODO this could be improved by iteration the unavailableShiftConstraintList and using a hashmap
//            for (UnavailableShiftConstraint constraint : nurseRoster.getUnavailableShiftConstraintList()) {
//                if (constraint.getEmployee().equals(employee)) {
//                    unavailableShiftConstraintCount++;
//                }
//            }
//        }
//
//        public Employee getEmployee() {
//            return employee;
//        }
//
//        public int compareTo(EmployeeInitializationWeight other) {
//
//            return new CompareToBuilder()
//                    .append(other.employee.getCurriculumList().size(), employee.getCurriculumList().size()) // Descending
//                    .append(other.unavailableShiftConstraintCount, unavailableShiftConstraintCount) // Descending
//                    .append(other.employee.getEmployeeAssignmentSize(), employee.getEmployeeAssignmentSize()) // Descending
//                    .append(other.employee.getStudentSize(), employee.getStudentSize()) // Descending
//                    .append(other.employee.getMinWorkingDaySize(), employee.getMinWorkingDaySize()) // Descending
//                    .append(employee.getId(), other.employee.getId()) // Ascending
//                    .toComparison();
//        }
//
//    }
//
//    private class ShiftScoring implements Comparable<ShiftScoring> {
//
//        private Shift shift;
//        private Score score;
//
//        private ShiftScoring(Shift shift, Score score) {
//            this.shift = shift;
//            this.score = score;
//        }
//
//        public Shift getShift() {
//            return shift;
//        }
//
//        public Score getScore() {
//            return score;
//        }
//
//        public int compareTo(ShiftScoring other) {
//            return -new CompareToBuilder().append(score, other.score).toComparison();
//        }
//
//    }

}
