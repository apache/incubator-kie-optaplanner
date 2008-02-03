package org.drools.solver.examples.common.swingui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drools.solver.examples.common.business.ScoreDetail;
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
        List<ScoreDetail> scoreDetailList = solutionBusiness.getConstraintScoreMap();
        for (ScoreDetail scoreDetail : scoreDetailList) {
            JLabel constraintIdLabel = new JLabel(
                    scoreDetail.getRuleId() + " (" + scoreDetail.getConstraintType() + ")");
            constraintIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            panel.add(constraintIdLabel);
            JLabel constraintScoreLabel = new JLabel(
                    WorkflowFrame.NUMBER_FORMAT.format(scoreDetail.getScoreTotal())
                    + " (" + WorkflowFrame.NUMBER_FORMAT.format(scoreDetail.getOccurenceSize()) + ")");
            panel.add(constraintScoreLabel);
        }
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(getParent());
    }

}
