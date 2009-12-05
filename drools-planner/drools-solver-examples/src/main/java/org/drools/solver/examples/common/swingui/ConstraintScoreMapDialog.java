package org.drools.solver.examples.common.swingui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
        List<ScoreDetail> scoreDetailList = solutionBusiness.getScoreDetailList();
        JTable table = new JTable(new ScoreDetailTableModel(scoreDetailList));
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(700, 300));
        setContentPane(scrollpane);
        pack();
        setLocationRelativeTo(getParent());
    }

    public static class ScoreDetailTableModel extends AbstractTableModel {

        private List<ScoreDetail> scoreDetailList;

        public ScoreDetailTableModel(List<ScoreDetail> scoreDetailList) {
            this.scoreDetailList = scoreDetailList;
        }

        public int getRowCount() {
            return scoreDetailList.size();
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Rule id";
                case 1:
                    return "Constraint type";
                case 2:
                    return "# occurences";
                case 3:
                    return "Score total";
                default:
                    throw new IllegalStateException("The columnIndex (" + columnIndex + ") is invalid.");
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Enum.class;
                case 2:
                    return Integer.class;
                case 3:
                    return Double.class;
                default:
                    throw new IllegalStateException("The columnIndex (" + columnIndex + ") is invalid.");
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            ScoreDetail scoreDetail = scoreDetailList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return scoreDetail.getRuleId();
                case 1:
                    return scoreDetail.getConstraintType();
                case 2:
                    return scoreDetail.getOccurenceSize();
                case 3:
                    return scoreDetail.getScoreTotal();
                default:
                    throw new IllegalStateException("The columnIndex (" + columnIndex + ") is invalid.");
            }
        }
    }

}
