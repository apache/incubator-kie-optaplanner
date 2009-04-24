package org.drools.solver.examples.common.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.drools.solver.examples.common.business.SolutionBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class WorkflowFrame extends JFrame {
    
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolutionBusiness solutionBusiness;

    private SolutionPanel solutionPanel;
    private JLabel resultLabel;
    private ConstraintScoreMapDialog constraintScoreMapDialog;

    public WorkflowFrame(SolutionPanel solutionPanel, String exampleName) {
        super("Drools solver example " + exampleName);
        this.solutionPanel = solutionPanel;
        solutionPanel.setWorkflowFrame(this);
        constraintScoreMapDialog = new ConstraintScoreMapDialog(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setSolutionBusiness(SolutionBusiness solutionBusiness) {
        this.solutionBusiness = solutionBusiness;
        solutionPanel.setSolutionBusiness(solutionBusiness);
        constraintScoreMapDialog.setSolutionBusiness(solutionBusiness);
    }

    public void init() {
        setContentPane(createContentPane());
        pack();
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createButtonPanel(), BorderLayout.NORTH);
        JScrollPane solutionScrollPane = new JScrollPane(solutionPanel);
        panel.add(solutionScrollPane, BorderLayout.CENTER);
        panel.add(createScorePanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0));
        panel.add(createLoadUnsolvedPanel());
        panel.add(createLoadSolvedPanel());
        panel.add(createProcessingPanel());
        return panel;
    }

    private JComponent createLoadUnsolvedPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (File file : solutionBusiness.getUnsolvedFileList()) {
            Action action = new LoadAction(file);
            panel.add(new JButton(action));
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        return scrollPane;
    }

    private JComponent createLoadSolvedPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (File file : solutionBusiness.getSolvedFileList()) {
            Action action = new LoadAction(file);
            panel.add(new JButton(action));
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        return scrollPane;
    }

    private class LoadAction extends AbstractAction {

        private File file;

        public LoadAction(File file) {
            super("Load " + file.getName());
            this.file = file;
        }

        public void actionPerformed(ActionEvent e) {
            solutionBusiness.load(file);
            updateScreen();
        }

    }

    private JComponent createProcessingPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        Action solveAction = new SolveAction();
        panel.add(new JButton(solveAction));
        Action saveAction = new SaveAction();
        panel.add(new JButton(saveAction));
        return panel;
    }

    private class SolveAction extends AbstractAction {

        public SolveAction() {
            super("Solve!");
        }

        public void actionPerformed(ActionEvent e) {
            solutionBusiness.solve();
            updateScreen();
        }

    }

    private class SaveAction extends AbstractAction {

        public SaveAction() {
            super("Save as...");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getSolvedDataDir());
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xml");
                }
                public String getDescription() {
                    return "Solver xml files";
                }
            });
            int approved = fileChooser.showOpenDialog(WorkflowFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                solutionBusiness.save(fileChooser.getSelectedFile());
            }
        }

    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("No solution loaded yet");
        resultLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(resultLabel, BorderLayout.CENTER);
        JButton constraintScoreMapButton = new JButton(new ShowConstraintScoreMapDialogAction());

        panel.add(constraintScoreMapButton, BorderLayout.EAST);
        return panel;
    }

    private class ShowConstraintScoreMapDialogAction extends AbstractAction {

        public ShowConstraintScoreMapDialogAction() {
            super("Constraint scores");
        }

        public void actionPerformed(ActionEvent e) {
            constraintScoreMapDialog.resetContentPanel();
            constraintScoreMapDialog.setVisible(true);
        }

    }

    public void updateScreen() {
        solutionPanel.resetPanel();
        validate();
        resultLabel.setText("Score = " + solutionBusiness.getScore());
    }

}
