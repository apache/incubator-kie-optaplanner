package org.drools.solver.examples.itc2007.curriculumcourse.app;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.CommonApp;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.itc2007.curriculumcourse.swingui.CurriculumCoursePanel;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/itc2007/curriculumcourse/solver/curriculumCourseSolverConfig.xml";

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