package org.drools.planner.examples.nurserostering.solver.move.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.core.move.CompositeMove;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.nurserostering.domain.Assignment;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.planner.examples.nurserostering.domain.solver.EmployeeWorkSequence;
import org.drools.planner.examples.nurserostering.solver.move.EmployeeChangeMove;
import org.drools.planner.examples.pas.domain.Bed;
import org.drools.planner.examples.pas.domain.BedDesignation;
import org.drools.planner.examples.pas.solver.move.BedChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class AssignmentSequenceSwitchMoveFactory extends AbstractMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        NurseRoster nurseRoster = (NurseRoster) solution;
        List<Employee> employeeList = nurseRoster.getEmployeeList();
        // This code assumes the assignmentList is sorted
        List<Assignment> assignmentList = nurseRoster.getAssignmentList();

        // Hash the assignments per employee
        Map<Employee, List<AssignmentSequence>> employeeToAssignmentSequenceListMap
                = new HashMap<Employee, List<AssignmentSequence>>(employeeList.size());
        int assignmentSequenceCapacity = nurseRoster.getShiftDateList().size() + 1 / 2;
        for (Employee employee : employeeList) {
            employeeToAssignmentSequenceListMap.put(employee,
                    new ArrayList<AssignmentSequence>(assignmentSequenceCapacity));
        }
        for (Assignment assignment : assignmentList) {
            Employee employee = assignment.getEmployee();
            List<AssignmentSequence> assignmentSequenceList = employeeToAssignmentSequenceListMap.get(employee);
            if (assignmentSequenceList.isEmpty()) {
                AssignmentSequence assignmentSequence = new AssignmentSequence(assignment);
                assignmentSequenceList.add(assignmentSequence);
            } else {
                AssignmentSequence lastAssignmentSequence = assignmentSequenceList // getLast()
                        .get(assignmentSequenceList.size() - 1);
                if (lastAssignmentSequence.belongsHere(assignment)) {
                    lastAssignmentSequence.add(assignment);
                } else {
                    AssignmentSequence assignmentSequence = new AssignmentSequence(assignment);
                    assignmentSequenceList.add(assignmentSequence);
                }
            }
        }

        // The create the move list
        List<Move> moveList = new ArrayList<Move>();
        // For every 2 distinct employees
        for (ListIterator<Employee> leftEmployeeIt = employeeList.listIterator(); leftEmployeeIt.hasNext();) {
            Employee leftEmployee = leftEmployeeIt.next();
            List<AssignmentSequence> leftAssignmentSequenceList
                    = employeeToAssignmentSequenceListMap.get(leftEmployee);
            for (ListIterator<Employee> rightEmployeeIt = employeeList.listIterator(leftEmployeeIt.nextIndex());
                    rightEmployeeIt.hasNext();) {
                Employee rightEmployee = rightEmployeeIt.next();
                List<AssignmentSequence> rightAssignmentSequenceList
                        = employeeToAssignmentSequenceListMap.get(rightEmployee);

                for (AssignmentSequence leftAssignmentSequence : leftAssignmentSequenceList) {
                    List<Assignment> leftAssignmentList = leftAssignmentSequence.getAssignmentList();
                    for (AssignmentSequence rightAssignmentSequence : rightAssignmentSequenceList) {
                        List<Assignment> rightAssignmentList = rightAssignmentSequence.getAssignmentList();
                        // Only if not covered by AssignmentSwitchMoveFactory
                        if (leftAssignmentList.size() > 1 || rightAssignmentList.size() > 1) {
                            int pillarSize = 2; // TODO
                            List<Move> subMoveList = new ArrayList<Move>(pillarSize * 2);
                            for (Assignment leftAssignment : leftAssignmentList
                                    .subList(0, Math.min(pillarSize, leftAssignmentList.size()))) {
                                subMoveList.add(new EmployeeChangeMove(leftAssignment, rightEmployee));
                            }
                            for (Assignment rightAssignment : rightAssignmentList
                                    .subList(0, Math.min(pillarSize, rightAssignmentList.size()))) {
                                subMoveList.add(new EmployeeChangeMove(rightAssignment, leftEmployee));
                            }
                            moveList.add(new CompositeMove(subMoveList));
                        }
                    }
                }
            }
        }
        return moveList;
    }

    /**
     * TODO DRY with {@link EmployeeWorkSequence}
     */
    private static class AssignmentSequence {

        private List<Assignment> assignmentList;
        private int firstDayIndex;
        private int lastDayIndex;

        private AssignmentSequence(Assignment assignment) {
            assignmentList = new ArrayList<Assignment>();
            firstDayIndex = assignment.getShiftDateDayIndex();
            lastDayIndex = firstDayIndex;
        }

        public List<Assignment> getAssignmentList() {
            return assignmentList;
        }

        private void add(Assignment assignment) {
            assignmentList.add(assignment);
            int dayIndex = assignment.getShiftDateDayIndex();
            if (dayIndex < lastDayIndex) {
                throw new IllegalStateException("The assignmentList is expected to be sorted by shiftDate.");
            }
            lastDayIndex = dayIndex;
        }

        private boolean belongsHere(Assignment assignment) {
            return assignment.getShiftDateDayIndex() <= (lastDayIndex + 1);
        }

    }

}
