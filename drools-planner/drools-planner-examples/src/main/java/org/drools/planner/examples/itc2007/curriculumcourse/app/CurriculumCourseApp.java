package org.drools.planner.examples.itc2007.curriculumcourse.app;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.app.CommonApp;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.itc2007.curriculumcourse.swingui.CurriculumCoursePanel;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/planner/examples/itc2007/curriculumcourse/solver/curriculumCourseSolverConfig.xml";

    public static void main(String[] args) {
        new CurriculumCourseApp().init();
    }

    @Override
    protected Solver createSolver() {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        return configurer.buildSolver();
    }

    @Override
    protected String getExampleDirName() {
        return "itc2007/curriculumcourse";
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new CurriculumCoursePanel();
    }

}