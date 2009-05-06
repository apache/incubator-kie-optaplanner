package org.drools.solver.examples.common.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

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
import javax.swing.SwingUtilities;
import javax.swing.JProgressBar;
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
    private ConstraintScoreMapDialog constraintScoreMapDialog;

    private List<Action> loadUnsolvedActionList;
    private List<Action> loadSolvedActionList;
    private Action cancelSolvingAction;
    private Action solveAction;
    private Action saveAction;
    
    private JProgressBar progressBar;
    private JLabel resultLabel;
    private ShowConstraintScoreMapDialogAction showConstraintScoreMapDialogAction;

    public WorkflowFrame(SolutionBusiness solutionBusiness, SolutionPanel solutionPanel, String exampleName) {
        super("Drools solver example " + exampleName);
        this.solutionBusiness = solutionBusiness;
        this.solutionPanel = solutionPanel;
        solutionPanel.setSolutionBusiness(solutionBusiness);
        solutionPanel.setWorkflowFrame(this);
        constraintScoreMapDialog = new ConstraintScoreMapDialog(this);
        constraintScoreMapDialog.setSolutionBusiness(solutionBusiness);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        loadUnsolvedActionList = new ArrayList<Action>();
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (File file : solutionBusiness.getUnsolvedFileList()) {
            Action loadUnsolvedAction = new LoadAction(file);
            loadUnsolvedActionList.add(loadUnsolvedAction);
            panel.add(new JButton(loadUnsolvedAction));
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        return scrollPane;
    }

    private JComponent createLoadSolvedPanel() {
        loadSolvedActionList = new ArrayList<Action>();
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (File file : solutionBusiness.getSolvedFileList()) {
            Action loadSolvedAction = new LoadAction(file);
            loadSolvedActionList.add(loadSolvedAction);
            panel.add(new JButton(loadSolvedAction));
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
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
            solveAction.setEnabled(true);
            saveAction.setEnabled(true);
            updateScreen();
        }

    }

    private JComponent createProcessingPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        solveAction = new SolveAction();
        solveAction.setEnabled(false);
        panel.add(new JButton(solveAction));
        cancelSolvingAction = new CancelSolvingAction();
        cancelSolvingAction.setEnabled(false);
        panel.add(new JButton(cancelSolvingAction));
        saveAction = new SaveAction();
        saveAction.setEnabled(false);
        panel.add(new JButton(saveAction));
        return panel;
    }

    private void setSolvingState(boolean solving) {
        for (Action action : loadUnsolvedActionList) {
            action.setEnabled(!solving);
        }
        for (Action action : loadSolvedActionList) {
            action.setEnabled(!solving);
        }
        solveAction.setEnabled(!solving);
        cancelSolvingAction.setEnabled(solving);
        saveAction.setEnabled(!solving);
        solutionPanel.setEnabled(!solving);
        progressBar.setIndeterminate(solving);
        progressBar.setStringPainted(solving);
        progressBar.setString(solving ?  "Solving..." : null);
        showConstraintScoreMapDialogAction.setEnabled(!solving);
    }

    private class SolveAction extends AbstractAction {
        
        // This should be replaced with a java 6 SwingWorker once drools's hudson is on JDK 1.6
        private ExecutorService solvingExecutor = Executors.newFixedThreadPool(1);

        public SolveAction() {
            super("Solve!");
        }

        public void actionPerformed(ActionEvent e) {
            setSolvingState(true);
            // This should be replaced with a java 6 SwingWorker once drools's hudson is on JDK 1.6
            solvingExecutor.submit(new Runnable() {
                public void run() {
                    solutionBusiness.solve();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setSolvingState(false);
                            updateScreen();
                        }
                    });
                }
            });
        }

    }

    private class CancelSolvingAction extends AbstractAction {

        public CancelSolvingAction() {
            super("Cancel solving");
        }

        public void actionPerformed(ActionEvent e) {
            // This async, so it doesn't stop the solving immediatly
            solutionBusiness.cancelSolving();
        }

    }

    private class SaveAction extends AbstractAction {

        public SaveAction() {
            super("Save as...");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getSolvedDataDir());
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xml");
                }
                public String getDescription() {
                    return "Solver xml files";
                }
            });
            int approved = fileChooser.showSaveDialog(WorkflowFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                solutionBusiness.save(fileChooser.getSelectedFile());
            }
        }

    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        panel.add(progressBar, BorderLayout.WEST);
        resultLabel = new JLabel("No solution loaded yet");
        resultLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(resultLabel, BorderLayout.CENTER);
        showConstraintScoreMapDialogAction = new ShowConstraintScoreMapDialogAction();
        JButton constraintScoreMapButton = new JButton(showConstraintScoreMapDialogAction);
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
