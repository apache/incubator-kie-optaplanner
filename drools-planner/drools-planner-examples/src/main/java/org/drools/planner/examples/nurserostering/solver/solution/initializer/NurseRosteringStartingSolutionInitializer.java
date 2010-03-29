package org.drools.planner.examples.nurserostering.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.DefaultHardAndSoftScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.runtime.rule.FactHandle;

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
        initializeEmployeeAssignmentList(localSearchSolverScope, schedule);
    }

    private void initializeEmployeeAssignmentList(LocalSearchSolverScope localSearchSolverScope,
            NurseRoster nurseRoster) {
        List<Shift> shiftList = nurseRoster.getShiftList();
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();

        List<EmployeeAssignment> employeeAssignmentList = createEmployeeAssignmentList(nurseRoster);
        // TODO implement this class
//        for (EmployeeAssignment employeeAssignment : employeeAssignmentList) {
//            Score unnurseRosterdScore = localSearchSolverScope.calculateScoreFromWorkingMemory();
//            FactHandle employeeAssignmentHandle = null;
//
//            List<ShiftScoring> shiftScoringList = new ArrayList<ShiftScoring>(shiftList.size());
//            for (Shift shift : shiftList) {
//                if (employeeAssignmentHandle == null) {
//                    employeeAssignment.setShift(shift);
//                    employeeAssignmentHandle = workingMemory.insert(employeeAssignment);
//                } else {
//                    employeeAssignment.setShift(shift);
//                    workingMemory.update(employeeAssignmentHandle, employeeAssignment);
//                }
//                Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
//                shiftScoringList.add(new ShiftScoring(shift, score));
//            }
//            Collections.sort(shiftScoringList);
//
//            boolean almostPerfectMatch = false;
//            Score bestScore = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE, Integer.MIN_VALUE);
//            Shift bestShift = null;
//            Room bestRoom = null;
//            for (ShiftScoring shiftScoring : shiftScoringList) {
//                if (bestScore.compareTo(shiftScoring.getScore()) >= 0) {
//                    // No need to check the rest
//                    break;
//                }
//                employeeAssignment.setShift(shiftScoring.getShift());
//                workingMemory.update(employeeAssignmentHandle, employeeAssignment);
//
//                for (Room room : roomList) {
//                    employeeAssignment.setRoom(room);
//                    workingMemory.update(employeeAssignmentHandle, employeeAssignment);
//                    Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
//                    if (score.compareTo(unnurseRosterdScore) < 0) {
//                        if (score.compareTo(bestScore) > 0) {
//                            bestScore = score;
//                            bestShift = shiftScoring.getShift();
//                            bestRoom = room;
//                        }
//                    } else if (score.compareTo(unnurseRosterdScore) >= 0) {
//                        // TODO due to the score rules, the score can unnurseRosterdScore can be higher than the score
//                        // In theory every possibility should be looked into
//                        almostPerfectMatch = true;
//                        break;
//                    }
//                }
//                if (almostPerfectMatch) {
//                    break;
//                }
//            }
//            if (!almostPerfectMatch) {
//                if (bestShift == null || bestRoom == null) {
//                    throw new IllegalStateException("The bestShift (" + bestShift + ") or the bestRoom ("
//                            + bestRoom + ") cannot be null.");
//                }
//                employeeAssignment.setShift(bestShift);
//                employeeAssignment.setRoom(bestRoom);
//                workingMemory.update(employeeAssignmentHandle, employeeAssignment);
//            }
//            logger.debug("    EmployeeAssignment ({}) initialized for starting solution.", employeeAssignment);
//        }
        
        // TODO tmp begin
        for (EmployeeAssignment employeeAssignment : employeeAssignmentList) {
            workingMemory.insert(employeeAssignment);
        }
        // TODO tmp end

        Collections.sort(employeeAssignmentList, new PersistableIdComparator());
        nurseRoster.setEmployeeAssignmentList(employeeAssignmentList);
    }

    public List<EmployeeAssignment> createEmployeeAssignmentList(NurseRoster nurseRoster) {
        List<Employee> employeeList = nurseRoster.getEmployeeList();

//        List<EmployeeInitializationWeight> employeeInitializationWeightList
//                = new ArrayList<EmployeeInitializationWeight>(employeeList.size());
//        for (Employee employee : employeeList) {
//            employeeInitializationWeightList.add(new EmployeeInitializationWeight(nurseRoster, employee));
//        }
//        Collections.sort(employeeInitializationWeightList);
//
        List<EmployeeAssignment> employeeAssignmentList = new ArrayList<EmployeeAssignment>(employeeList.size() * 5);
//        int employeeAssignmentId = 0;
//        for (EmployeeInitializationWeight employeeInitializationWeight : employeeInitializationWeightList) {
//            Employee employee = employeeInitializationWeight.getEmployee();
//            for (int i = 0; i < employee.getEmployeeAssignmentSize(); i++) {
//                EmployeeAssignment employeeAssignment = new EmployeeAssignment();
//                employeeAssignment.setId((long) employeeAssignmentId);
//                employeeAssignmentId++;
//                employeeAssignment.setEmployee(employee);
//                employeeAssignment.setEmployeeAssignmentIndexInEmployee(i);
//                employeeAssignmentList.add(employeeAssignment);
//            }
//        }

        // TODO tmp begin
        List<ShiftDate> shiftDateList = nurseRoster.getShiftDateList();
        int employeeAssignmentId = 0;
        Random random = new Random(); // not seeded, tmp!
        for (Employee employee : employeeList) {
            for (ShiftDate shiftDate : shiftDateList) {
                EmployeeAssignment employeeAssignment = new EmployeeAssignment();
                employeeAssignment.setId((long) employeeAssignmentId);
                employeeAssignmentId++;
                employeeAssignment.setEmployee(employee);
                employeeAssignment.setShiftDate(shiftDate);
                List<Shift> shiftList = shiftDate.getShiftList();
                int randomInt = random.nextInt(shiftList.size() + 1);
                employeeAssignment.setShift(randomInt == shiftList.size() ? null : shiftList.get(randomInt));
                employeeAssignmentList.add(employeeAssignment);
            }
        }
        // TODO tmp end

        return employeeAssignmentList;
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
