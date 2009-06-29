package org.drools.solver.examples.pas.swingui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;

import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.pas.domain.PatientAdmissionSchedule;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedulePanel extends SolutionPanel {

    private static final Color HEADER_COLOR = Color.YELLOW;

    private GridLayout gridLayout;

    public PatientAdmissionSchedulePanel() {
        gridLayout = new GridLayout(0, 1);
        setLayout(gridLayout);
        add(new JLabel("GUI TODO"));
    }

    private PatientAdmissionSchedule getPatientAdmissionSchedule() {
        return (PatientAdmissionSchedule) solutionBusiness.getSolution();
    }

    public void resetPanel() {
        removeAll();
        PatientAdmissionSchedule patientAdmissionSchedule = getPatientAdmissionSchedule();
//        gridLayout.setColumns(patientAdmissionSchedule.getRoomList().size() + 1);
//        JLabel headerCornerLabel = new JLabel("Period         \\         Room");
//        headerCornerLabel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.DARK_GRAY),
//                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
//        headerCornerLabel.setBackground(HEADER_COLOR);
//        headerCornerLabel.setOpaque(true);
//        add(headerCornerLabel);
//        for (Room room : patientAdmissionSchedule.getRoomList()) {
//            JLabel roomLabel = new JLabel(room.toString());
//            roomLabel.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(Color.DARK_GRAY),
//                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
//            roomLabel.setBackground(HEADER_COLOR);
//            roomLabel.setOpaque(true);
//            add(roomLabel);
//        }
//        Map<Period, Map<Room, PeriodRoomPanel>> periodRoomPanelMap = new HashMap<Period, Map<Room, PeriodRoomPanel>>();
//        for (Period period : patientAdmissionSchedule.getPeriodList()) {
//            JLabel periodLabel = new JLabel(period.toString() + " " + period.getStartDateTimeString());
//            periodLabel.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(Color.DARK_GRAY),
//                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
//            periodLabel.setBackground(HEADER_COLOR);
//            periodLabel.setOpaque(true);
//            add(periodLabel);
//            Map<Room, PeriodRoomPanel> roomPanelMap = new HashMap<Room, PeriodRoomPanel>();
//            periodRoomPanelMap.put(period, roomPanelMap);
//            for (Room room : patientAdmissionSchedule.getRoomList()) {
//                PeriodRoomPanel periodRoomPanel = new PeriodRoomPanel();
//                add(periodRoomPanel);
//                roomPanelMap.put(room, periodRoomPanel);
//            }
//        }
//        if (patientAdmissionSchedule.isInitialized()) {
//            for (Exam exam : patientAdmissionSchedule.getExamList()) {
//                PeriodRoomPanel periodRoomPanel = periodRoomPanelMap.get(exam.getPeriod()).get(exam.getRoom());
//                periodRoomPanel.addExam(exam);
//            }
//        }
    }

//    private class PeriodRoomPanel extends JPanel {
//
//        public PeriodRoomPanel() {
//            super(new GridLayout(0, 1));
//            setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(Color.DARK_GRAY),
//                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
//        }
//
//        public void addExam(Exam exam) {
//            JButton button = new JButton(new ExamAction(exam));
//            add(button);
//        }
//
//    }
//
//    private class ExamAction extends AbstractAction {
//
//        private Exam exam;
//
//        public ExamAction(Exam exam) {
//            super(exam.getTopic().toString());
//            this.exam = exam;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            JPanel listFieldsPanel = new JPanel(new GridLayout(2, 1));
//            List<Period> periodList = getPatientAdmissionSchedule().getPeriodList();
//            JComboBox periodListField = new JComboBox(periodList.toArray());
//            periodListField.setSelectedItem(exam.getPeriod());
//            listFieldsPanel.add(periodListField);
//            List<Room> roomList = getPatientAdmissionSchedule().getRoomList();
//            JComboBox roomListField = new JComboBox(roomList.toArray());
//            roomListField.setSelectedItem(exam.getRoom());
//            listFieldsPanel.add(roomListField);
//            int result = JOptionPane.showConfirmDialog(PatientAdmissionSchedulePanel.this.getRootPane(), listFieldsPanel,
//                    "Select period and room", JOptionPane.OK_CANCEL_OPTION);
//            if (result == JOptionPane.OK_OPTION) {
//                Period toPeriod = (Period) periodListField.getSelectedItem();
//                solutionBusiness.doMove(new PeriodChangeMove(exam, toPeriod));
//                Room toRoom = (Room) roomListField.getSelectedItem();
//                solutionBusiness.doMove(new RoomChangeMove(exam, toRoom));
//                workflowFrame.updateScreen();
//            }
//        }
//
//    }

}