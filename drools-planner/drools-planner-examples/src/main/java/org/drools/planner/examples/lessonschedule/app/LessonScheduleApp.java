package org.drools.planner.examples.lessonschedule.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.lessonschedule.persistence.LessonScheduleDaoImpl;
import org.drools.planner.examples.lessonschedule.swingui.LessonSchedulePanel;
import org.drools.planner.examples.manners2009.persistence.Manners2009DaoImpl;

/**
 * @author Geoffrey De Smet
 */
public class LessonScheduleApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/lessonschedule/solver/lessonScheduleSolverConfig.xml";

    public static void main(String[] args) {
        new LessonScheduleApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new LessonSchedulePanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new LessonScheduleDaoImpl();
    }

}
