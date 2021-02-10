package org.optaplanner.examples.batchscheduling.app;

import org.optaplanner.examples.batchscheduling.domain.Schedule;
import org.optaplanner.examples.batchscheduling.swingui.BatchSchedulingPanel;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

public class BatchSchedulingApp extends CommonApp<Schedule> {

    public static final String SOLVER_CONFIG =
            "org/optaplanner/examples/batchscheduling/solver/batchSchedulingSolverConfig.xml";
    public static final String DATA_DIR_NAME = "batchscheduling";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new BatchSchedulingApp().init();
    }

    @Override
    public SolutionFileIO<Schedule> createSolutionFileIO() {
        return new XStreamSolutionFileIO<>(BatchSchedulingApp.class);
    }

    public BatchSchedulingApp() {
        super("Batch Scheduling",
                "Official competition name:" +
                        " multi-mode resource-constrained multi-project scheduling problem (MRCMPSP)\n\n" +
                        "Schedule all batches.\n\n" +
                        "Minimize delivery delays.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                BatchSchedulingPanel.LOGO_PATH);
    }

    @Override
    protected SolutionPanel<Schedule> createSolutionPanel() {
        return new BatchSchedulingPanel();
    }

    /*
     * @Override
     * protected AbstractSolutionImporter[] createSolutionImporters() {
     * return new AbstractSolutionImporter[]{
     * new BatchSchedulingImporter()
     * };
     * }
     */
}
