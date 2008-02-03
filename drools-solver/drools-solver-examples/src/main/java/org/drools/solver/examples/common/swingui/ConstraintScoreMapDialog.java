package org.drools.solver.examples.common.swingui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
        JPanel panel = new JPanel(new GridLayout(0, 4));
        List<ScoreDetail> scoreDetailList = solutionBusiness.getScoreDetailList();
        JLabel ruleIdHeader = new JLabel("Rule id");
        ruleIdHeader.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(ruleIdHeader);
        JLabel constraintTypeHeader = new JLabel("Constraint type");
        constraintTypeHeader.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(constraintTypeHeader);
        JLabel occurenceSizeHeader = new JLabel("# occurences");
        occurenceSizeHeader.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(occurenceSizeHeader);
        JLabel scoreTotalHeader = new JLabel("Score total");
        scoreTotalHeader.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(scoreTotalHeader);
        for (ScoreDetail scoreDetail : scoreDetailList) {
            JLabel ruleIdLabel = new JLabel(scoreDetail.getRuleId());
            ruleIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            panel.add(ruleIdLabel);
            JLabel constraintTypeLabel = new JLabel(scoreDetail.getConstraintType().toString());
            constraintTypeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            panel.add(constraintTypeLabel);
            JLabel occurenceSizeLabel = new JLabel(WorkflowFrame.NUMBER_FORMAT.format(scoreDetail.getOccurenceSize()));
            occurenceSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            occurenceSizeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            panel.add(occurenceSizeLabel);
            JLabel scoreTotalLabel = new JLabel(WorkflowFrame.NUMBER_FORMAT.format(scoreDetail.getScoreTotal()));
            scoreTotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            panel.add(scoreTotalLabel);
        }
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(getParent());
    }

}
