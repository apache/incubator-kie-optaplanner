package org.drools.solver.examples.itc2007.examination.swingui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationPanel extends SolutionPanel {

    private static final Color HEADER_COLOR = Color.YELLOW;

    private GridLayout gridLayout;

    public ExaminationPanel() {
        super();
        gridLayout = new GridLayout(0, 1);
        setLayout(gridLayout);
    }

    private Examination getExamination() {
        return (Examination) solutionBusiness.getSolution();
    }

    public void resetPanel() {
        removeAll();
        Examination examination = getExamination();
        gridLayout.setColumns(examination.getRoomList().size() + 1);
        JPanel headerCorner = new JPanel();
        headerCorner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        headerCorner.setBackground(HEADER_COLOR);
        add(headerCorner);
        for (Room room : examination.getRoomList()) {
            JLabel roomLabel = new JLabel(room.toString());
            roomLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            roomLabel.setBackground(HEADER_COLOR);
            roomLabel.setOpaque(true);
            add(roomLabel);
        }
        Map<Period, Map<Room, PeriodRoomPanel>> periodRoomPanelMap = new HashMap<Period, Map<Room, PeriodRoomPanel>>();
        for (Period period : examination.getPeriodList()) {
            JLabel periodLabel = new JLabel(period.toString());
            periodLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            periodLabel.setBackground(HEADER_COLOR);
            periodLabel.setOpaque(true);
            add(periodLabel);
            Map<Room, PeriodRoomPanel> roomPanelMap = new HashMap<Room, PeriodRoomPanel>();
            periodRoomPanelMap.put(period, roomPanelMap);
            for (Room room : examination.getRoomList()) {
                PeriodRoomPanel periodRoomPanel = new PeriodRoomPanel();
                add(periodRoomPanel);
                roomPanelMap.put(room, periodRoomPanel);
            }
        }
        for (Exam exam : examination.getExamList()) {
            PeriodRoomPanel periodRoomPanel = periodRoomPanelMap.get(exam.getPeriod()).get(exam.getRoom());
            periodRoomPanel.addExam(exam);
        }
    }

    private class PeriodRoomPanel extends JPanel {

        public PeriodRoomPanel() {
            super(new GridLayout(0, 1));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        public void addExam(Exam exam) {
            JButton button = new JButton(new ExamAction(exam));
            add(button);
        }

    }

    private class ExamAction extends AbstractAction {

        private Exam exam;

        public ExamAction(Exam exam) {
            super(exam.getTopic().toString());
            this.exam = exam;
        }

        public void actionPerformed(ActionEvent e) {
            // TODO
//            List<Day> dayList = getExamination().getDayList();
//            JComboBox dayListField = new JComboBox(dayList.toArray());
//            dayListField.setSelectedItem(match.getDay());
//            int result = JOptionPane.showConfirmDialog(ExaminationPanel.this, dayListField, "Select day",
//                    JOptionPane.OK_CANCEL_OPTION);
//            if (result == JOptionPane.OK_OPTION) {
//                Day toDay = (Day) dayListField.getSelectedItem();
//                Move move = new DayChangeMove(match, toDay);
//                solutionBusiness.doMove(move);
//                workflowFrame.updateScreen();
//            }
        }

    }

}