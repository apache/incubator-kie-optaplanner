package org.drools.planner.examples.nurserostering.swingui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.nurserostering.domain.DayOfWeek;
import org.drools.planner.examples.nurserostering.domain.ShiftDate;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.solver.move.EmployeeChangeMove;

/**
 * TODO this code is highly unoptimized
 * @author Geoffrey De Smet
 */
public class NurseRosteringPanel extends SolutionPanel {

    private static final Color HEADER_COLOR = Color.YELLOW;

    private GridLayout gridLayout;

    public NurseRosteringPanel() {
        gridLayout = new GridLayout(0, 1);
        setLayout(gridLayout);
    }

    private NurseRoster getNurseRoster() {
        return (NurseRoster) solutionBusiness.getSolution();
    }

    public void resetPanel() {
        removeAll();
        NurseRoster schedule = getNurseRoster();
        gridLayout.setColumns(schedule.getShiftDateList().size() + 1);
        JLabel headerCornerLabel = new JLabel("Employee \\ Date");
        headerCornerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        headerCornerLabel.setBackground(HEADER_COLOR);
        headerCornerLabel.setOpaque(true);
        add(headerCornerLabel);
        for (ShiftDate shiftDate : schedule.getShiftDateList()) {
            JLabel shiftDateLabel = new JLabel(shiftDate.toString());
            shiftDateLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            shiftDateLabel.setBackground(HEADER_COLOR);
            shiftDateLabel.setOpaque(true);
            add(shiftDateLabel);
        }
        Map<Employee, Map<ShiftDate, EmployeeShiftDatePanel>> employeeShiftDatePanelMap = new HashMap<Employee, Map<ShiftDate, EmployeeShiftDatePanel>>();
        for (Employee employee : schedule.getEmployeeList()) {
            JLabel employeeLabel = new JLabel(employee.toString());
            employeeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            employeeLabel.setBackground(HEADER_COLOR);
            employeeLabel.setOpaque(true);
            add(employeeLabel);
            Map<ShiftDate, EmployeeShiftDatePanel> shiftDatePanelMap = new HashMap<ShiftDate, EmployeeShiftDatePanel>();
            employeeShiftDatePanelMap.put(employee, shiftDatePanelMap);
            for (ShiftDate shiftDate : schedule.getShiftDateList()) {
                EmployeeShiftDatePanel employeeShiftDatePanel = new EmployeeShiftDatePanel();
                if (shiftDate.getDayOfWeek() == DayOfWeek.SATURDAY || shiftDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    employeeShiftDatePanel.setBackground(Color.LIGHT_GRAY);
                }
                add(employeeShiftDatePanel);
                shiftDatePanelMap.put(shiftDate, employeeShiftDatePanel);
            }
        }
        if (schedule.isInitialized()) {
            for (EmployeeAssignment employeeAssignment : schedule.getEmployeeAssignmentList()) {
                Employee employee = employeeAssignment.getEmployee();
                EmployeeShiftDatePanel employeeShiftDatePanel = employeeShiftDatePanelMap.get(employee).get(employeeAssignment.getShiftDate());
                employeeShiftDatePanel.addEmployeeAssignment(employeeAssignment);
            }
        }
    }

    private class EmployeeShiftDatePanel extends JPanel {

        public EmployeeShiftDatePanel() {
            super(new GridLayout(0, 1));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        public void addEmployeeAssignment(EmployeeAssignment employeeAssignment) {
            JButton button = new JButton(new EmployeeAssignmentAction(employeeAssignment));
            add(button);
        }

    }

    private class EmployeeAssignmentAction extends AbstractAction {

        private EmployeeAssignment employeeAssignment;

        public EmployeeAssignmentAction(EmployeeAssignment employeeAssignment) {
            super(employeeAssignment.getLabel());
            this.employeeAssignment = employeeAssignment;
        }

        public void actionPerformed(ActionEvent e) {
            List<Employee> employeeList = getNurseRoster().getEmployeeList();
            JComboBox employeeListField = new JComboBox(employeeList.toArray());
            employeeListField.setSelectedItem(employeeAssignment.getEmployee());
            int result = JOptionPane.showConfirmDialog(NurseRosteringPanel.this.getRootPane(), employeeListField,
                    "Select employee", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Employee toEmployee = (Employee) employeeListField.getSelectedItem();
                solutionBusiness.doMove(new EmployeeChangeMove(employeeAssignment, toEmployee));
                workflowFrame.updateScreen();
            }
        }

    }

}
