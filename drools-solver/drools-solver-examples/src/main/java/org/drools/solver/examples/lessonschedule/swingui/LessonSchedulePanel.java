package org.drools.solver.examples.lessonschedule.swingui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.drools.solver.core.move.Move;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.lessonschedule.domain.Lesson;
import org.drools.solver.examples.lessonschedule.domain.LessonSchedule;
import org.drools.solver.examples.lessonschedule.domain.Timeslot;
import org.drools.solver.examples.lessonschedule.solver.move.TimeslotChangeMove;

/**
 * TODO this code is highly unoptimzed
 * @author Geoffrey De Smet
 */
public class LessonSchedulePanel extends SolutionPanel {

    public LessonSchedulePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private LessonSchedule getLessonSchedule() {
        return (LessonSchedule) solutionBusiness.getSolution();
    }

    public void resetPanel() {
        removeAll();
        LessonSchedule lessonSchedule = getLessonSchedule();
        Map<Timeslot, TimeslotPanel> timeslotPanelMap = new HashMap<Timeslot, TimeslotPanel>();
        for (Timeslot timeslot : lessonSchedule.getTimeslotList()) {
            TimeslotPanel timeslotPanel = new TimeslotPanel();
            add(timeslotPanel);
            timeslotPanelMap.put(timeslot,  timeslotPanel);
        }
        for (Lesson lesson : lessonSchedule.getLessonList()) {
            TimeslotPanel timeslotPanel = timeslotPanelMap.get(lesson.getTimeslot());
            timeslotPanel.addLesson(lesson);
        }
    }

    private class TimeslotPanel extends JPanel {

        public TimeslotPanel() {
            super(new GridLayout(0, 1));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }

        public void addLesson(Lesson lesson) {
            JButton button = new JButton(new LessonAction(lesson));
            add(button);
        }

    }

    private class LessonAction extends AbstractAction {

        private Lesson lesson;

        public LessonAction(Lesson lesson) {
            super(lesson.toString());
            this.lesson = lesson;
        }

        public void actionPerformed(ActionEvent e) {
            List<Timeslot> timeslotList = getLessonSchedule().getTimeslotList();
            JComboBox timeslotListField = new JComboBox(timeslotList.toArray());
            timeslotListField.setSelectedItem(lesson.getTimeslot());
            int result = JOptionPane.showConfirmDialog(LessonSchedulePanel.this.getRootPane(), timeslotListField,
                    "Select timeslot", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Timeslot toTimeslot = (Timeslot) timeslotListField.getSelectedItem();
                Move move = new TimeslotChangeMove(lesson, toTimeslot);
                solutionBusiness.doMove(move);
                workflowFrame.updateScreen();
            }
        }

    }

}
