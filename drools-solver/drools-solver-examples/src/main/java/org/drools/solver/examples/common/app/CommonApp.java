package org.drools.solver.examples.common.app;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.business.SolutionBusiness;
import org.drools.solver.examples.common.persistence.SolutionDao;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.swingui.SolutionPanel;
import org.drools.solver.examples.common.swingui.WorkflowFrame;

/**
 * @author Geoffrey De Smet
 */
public abstract class CommonApp {

    private static final String LOGGING_CONFIG = "/org/drools/solver/examples/common/app/log4j.xml";

    private WorkflowFrame workflowFrame;

    public CommonApp() {
        DOMConfigurator.configure(getClass().getResource(LOGGING_CONFIG));
        workflowFrame = new WorkflowFrame(createSolutionPanel(), getExampleDirName());
        workflowFrame.setSolutionBusiness(createSolutionBusiness());
        workflowFrame.init();
    }

    public void init() {
        workflowFrame.setVisible(true);
    }

    protected SolutionBusiness createSolutionBusiness() {
        SolutionBusiness solutionBusiness = new SolutionBusiness();
        solutionBusiness.setSolutionDao(createSolutionDao());
        solutionBusiness.setDataDir(new File("data/" + getExampleDirName()));
        solutionBusiness.setSolver(createSolver());
        return solutionBusiness;
    }

    protected SolutionDao createSolutionDao() {
        return new XstreamSolutionDaoImpl();
    }

    protected abstract String getExampleDirName();

    protected abstract Solver createSolver();

    protected abstract SolutionPanel createSolutionPanel();

}
