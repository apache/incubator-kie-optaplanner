package org.drools.solver.examples.common.swingui;

import javax.swing.JPanel;

import org.drools.solver.examples.common.business.SolutionBusiness;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author Geoffrey De Smet
 */
public abstract class SolutionPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected WorkflowFrame workflowFrame;
    protected SolutionBusiness solutionBusiness;

    public void setWorkflowFrame(WorkflowFrame workflowFrame) {
        this.workflowFrame = workflowFrame;
    }

    public void setSolutionBusiness(SolutionBusiness solutionBusiness) {
        this.solutionBusiness = solutionBusiness;
    }

    public abstract void resetPanel();
    
}
