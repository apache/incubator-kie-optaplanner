package org.drools.planner.examples.app;

import javax.swing.JOptionPane;

import org.drools.planner.examples.nqueens.app.NQueensApp;
import org.drools.planner.examples.lessonschedule.app.LessonScheduleApp;
import org.drools.planner.examples.travelingtournament.app.smart.SmartTravelingTournamentApp;
import org.drools.planner.examples.itc2007.examination.app.ExaminationApp;
import org.drools.planner.examples.itc2007.curriculumcourse.app.CurriculumCourseApp;
import org.drools.planner.examples.manners2009.app.Manners2009App;

/**
 * @author Geoffrey De Smet
 */
public class ExamplesApp {

    public static void main(String[] args) {
        String[] options = {
                "NQueens",
                "LessonSchedule",
                "TravelingTournament",
                "ITC2007 Examination",
                "ITC2007 CurriculumCourse",
                "Miss Manners 2009"
        };
        int choice = JOptionPane.showOptionDialog(null, "Which example do you want to see?", "Choose an example",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0 :
                new NQueensApp().init();
                break;
            case 1 :
                new LessonScheduleApp().init();
                break;
            case 2 :
                new SmartTravelingTournamentApp().init();
                break;
            case 3 :
                new ExaminationApp().init();
                break;
            case 4 :
                new CurriculumCourseApp().init();
                break;
            case 5 :
                new Manners2009App().init();
                break;
            default :
                throw new IllegalArgumentException("Unknown example choice");
        }
    }

}
