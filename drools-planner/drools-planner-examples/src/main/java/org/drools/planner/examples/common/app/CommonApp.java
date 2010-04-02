package org.drools.planner.examples.common.app;

import java.io.File;

import org.drools.planner.core.Solver;
import org.drools.planner.examples.common.business.SolutionBusiness;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.common.swingui.WorkflowFrame;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonApp extends LoggingMain {

    private WorkflowFrame workflowFrame;
    private SolutionDao solutionDao;
    private SolutionBusiness solutionBusiness;

    public CommonApp() {
        solutionDao = createSolutionDao();
        solutionBusiness = createSolutionBusiness();
        workflowFrame = new WorkflowFrame(solutionBusiness, createSolutionPanel(), solutionDao.getDirName());
    }

    public void init() {
        workflowFrame.init();
        workflowFrame.setVisible(true);
    }

    protected SolutionBusiness createSolutionBusiness() {
        SolutionBusiness solutionBusiness = new SolutionBusiness();
        solutionBusiness.setSolutionDao(solutionDao);
        solutionBusiness.setImporter(createSolutionImporter());
        solutionBusiness.setExporter(createSolutionExporter());
        solutionBusiness.setDataDir(solutionDao.getDataDir());
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
