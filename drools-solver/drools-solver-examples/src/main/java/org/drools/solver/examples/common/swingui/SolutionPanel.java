package org.drools.solver.examples.common.swingui;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.solver.examples.common.business.SolutionBusiness;

/**
 * @author Geoffrey De Smet
 */
public abstract class SolutionPanel extends JPanel {

    protected final transient Log log = LogFactory.getLog(getClass());

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
