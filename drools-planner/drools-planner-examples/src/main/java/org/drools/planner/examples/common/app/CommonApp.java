package org.drools.planner.examples.common.app;

import javax.swing.JFrame;

import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.business.SolutionBusiness;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.common.swingui.WorkflowFrame;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonApp extends LoggingMain {

    private WorkflowFrame workflowFrame;
    private SolutionBusiness solutionBusiness;

    public CommonApp() {
        solutionBusiness = createSolutionBusiness();
        workflowFrame = new WorkflowFrame(solutionBusiness, createSolutionPanel(), solutionBusiness.getDirName());
    }

    public void init() {
        init(true);
    }

    public void init(boolean exitOnClose) {
        workflowFrame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
        workflowFrame.init();
        workflowFrame.setVisible(true);
    }

    public SolutionBusiness createSolutionBusiness() {
        SolutionDao solutionDao = createSolutionDao();
        SolutionBusiness solutionBusiness = new SolutionBusiness();
        solutionBusiness.setSolutionDao(solutionDao);
        solutionBusiness.setImporter(createSolutionImporter());
        solutionBusiness.setExporter(createSolutionExporter());
        solutionBusiness.updateDataDirs();
        solutionBusiness.setSolver(createSolver());
        return solutionBusiness;
    }

    protected abstract Solver createSolver();

    protected abstract SolutionPanel createSolutionPanel();

    protected abstract SolutionDao createSolutionDao();

    protected AbstractSolutionImporter createSolutionImporter() {
        return null;
    }

    protected AbstractSolutionExporter createSolutionExporter() {
        return null;
    }

}
