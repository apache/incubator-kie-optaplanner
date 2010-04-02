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
import org.drools.planner.examples.nurserostering.domain.Employee;
import org.drools.planner.examples.nurserostering.domain.EmployeeAssignment;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;
import org.drools.planner.examples.nurserostering.domain.Shift;
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
        gridLayout.setColumns(schedule.getEmployeeList().size() + 1);
        JLabel headerCornerLabel = new JLabel("Shift     \\     Employee");
        headerCornerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        headerCornerLabel.setBackground(HEADER_COLOR);
        headerCornerLabel.setOpaque(true);
        add(headerCornerLabel);
        for (Employee employee : schedule.getEmployeeList()) {
            JLabel employeeLabel = new JLabel(employee.toString());
            employeeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            employeeLabel.setBackground(HEADER_COLOR);
            employeeLabel.setOpaque(true);
            add(employeeLabel);
        }
        Map<Shift, Map<Employee, ShiftEmployeePanel>> shiftEmployeePanelMap = new HashMap<Shift, Map<Employee, ShiftEmployeePanel>>();
        for (Shift shift : schedule.getShiftList()) {
            JLabel shiftLabel = new JLabel(shift.toString());
            shiftLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            shiftLabel.setBackground(HEADER_COLOR);
            shiftLabel.setOpaque(true);
            add(shiftLabel);
            Map<Employee, ShiftEmployeePanel> employeePanelMap = new HashMap<Employee, ShiftEmployeePanel>();
            shiftEmployeePanelMap.put(shift, employeePanelMap);
            for (Employee employee : schedule.getEmployeeList()) {
                ShiftEmployeePanel shiftEmployeePanel = new ShiftEmployeePanel();
                add(shiftEmployeePanel);
                employeePanelMap.put(employee, shiftEmployeePanel);
            }
        }
        if (schedule.isInitialized()) {
            for (EmployeeAssignment employeeAssignment : schedule.getEmployeeAssignmentList()) {
                Shift shift = employeeAssignment.getShift();
                ShiftEmployeePanel shiftEmployeePanel = shiftEmployeePanelMap.get(shift).get(employeeAssignment.getEmployee());
                shiftEmployeePanel.addEmployeeAssignment(employeeAssignment);
            }
        }
    }

    private class ShiftEmployeePanel extends JPanel {

        public ShiftEmployeePanel() {
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
            employeeListField.setSelectedItem(employeeAssignment.getShift());
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
