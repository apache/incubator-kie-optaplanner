package org.drools.planner.examples.app;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.drools.planner.examples.nqueens.app.NQueensApp;
import org.drools.planner.examples.lessonschedule.app.LessonScheduleApp;
import org.drools.planner.examples.pas.app.PatientAdmissionScheduleApp;
import org.drools.planner.examples.travelingtournament.app.smart.SmartTravelingTournamentApp;
import org.drools.planner.examples.examination.app.ExaminationApp;
import org.drools.planner.examples.curriculumcourse.app.CurriculumCourseApp;
import org.drools.planner.examples.manners2009.app.Manners2009App;

/**
 * @author Geoffrey De Smet
 */
public class ExamplesApp extends JFrame {

    public static void main(String[] args) {
        ExamplesApp examplesApp = new ExamplesApp();
        examplesApp.pack();
        examplesApp.setVisible(true);
    }

    public ExamplesApp() {
        super("Drools Planner examples");
        setContentPane(createContentPane());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private Container createContentPane() {
        JPanel contentPane = new JPanel(new GridLayout(0, 1));
        contentPane.add(new JLabel("Which example do you want to see?"));
        contentPane.add(new JButton(new AbstractAction("N queens"){
            public void actionPerformed(ActionEvent e) {
                new NQueensApp().init();
            }
        }));
        contentPane.add(new JButton(new AbstractAction("Miss Manners 2009"){
            public void actionPerformed(ActionEvent e) {
                new Manners2009App().init();
            }
        }));
        contentPane.add(new JButton(new AbstractAction("Traveling tournament"){
            public void actionPerformed(ActionEvent e) {
                new SmartTravelingTournamentApp().init();
            }
        }));
        contentPane.add(new JButton(new AbstractAction("ITC2007 Curriculum course timetabling"){
            public void actionPerformed(ActionEvent e) {
                new CurriculumCourseApp().init();
            }
        }));
        contentPane.add(new JButton(new AbstractAction("ITC2007 Examination timetabling"){
            public void actionPerformed(ActionEvent e) {
                new ExaminationApp().init();
            }
        }));
        contentPane.add(new JButton(new AbstractAction("Patient admission schedule"){
            public void actionPerformed(ActionEvent e) {
                new PatientAdmissionScheduleApp().init();
            }
        }));
        return contentPane;
    }

}
