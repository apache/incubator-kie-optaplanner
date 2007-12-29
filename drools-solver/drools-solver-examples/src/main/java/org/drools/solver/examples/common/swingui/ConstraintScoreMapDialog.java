package org.drools.solver.examples.common.swingui;

import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drools.solver.examples.common.business.SolutionBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class ConstraintScoreMapDialog extends JDialog {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected WorkflowFrame workflowFrame;
    protected SolutionBusiness solutionBusiness;

    public ConstraintScoreMapDialog(WorkflowFrame workflowFrame) {
        super(workflowFrame, "Constraint scores", true);
        this.workflowFrame = workflowFrame;
    }

    public void setSolutionBusiness(SolutionBusiness solutionBusiness) {
        this.solutionBusiness = solutionBusiness;
    }

    public void resetContentPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        Map<String, Double> scoreDetails = solutionBusiness.getConstraintScoreMap();
        for (Map.Entry<String, Double> scoreDetail : scoreDetails.entrySet()) {
            JLabel constraintIdLabel = new JLabel(scoreDetail.getKey());
            constraintIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            panel.add(constraintIdLabel);
            JLabel constraintScoreLabel = new JLabel(WorkflowFrame.NUMBER_FORMAT.format(scoreDetail.getValue()));
            panel.add(constraintScoreLabel);
        }
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(getParent());
    }

}
