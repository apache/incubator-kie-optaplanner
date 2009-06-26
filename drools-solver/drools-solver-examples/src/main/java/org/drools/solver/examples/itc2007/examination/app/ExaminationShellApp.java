package org.drools.solver.examples.itc2007.examination.app;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.persistence.ExaminationInputConvertor;
import org.drools.solver.examples.itc2007.examination.persistence.ExaminationOutputConvertor;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationShellApp extends LoggingMain {

    public static final String SOLVER_CONFIG
            = "/org/drools/solver/examples/itc2007/examination/solver/examinationSolverConfig.xml";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Use exactly 1 program argument (maximumSecondsSpend). Exiting...");
            System.exit(1);
        }
        long maximumSecondsSpend;
        try {
            maximumSecondsSpend = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("The program argument maximumSecondsSpend (" + args[0] + ") is not a number."
                    + " Exiting...");
            System.exit(1);
            maximumSecondsSpend = 0L; // unreachable statement
        }
        new ExaminationShellApp(maximumSecondsSpend).processs();
    }

    private ExaminationInputConvertor examinationInputConvertor = new ExaminationInputConvertor();
    private ExaminationOutputConvertor examinationOutputConvertor = new ExaminationOutputConvertor();

    private Solver solver;
    private File inputDir;
    private File outputDir;

    private ExaminationShellApp(long maximumSecondsSpend) {
        configureSolver(maximumSecondsSpend);
        configureDataDir();
    }

    private void configureSolver(long maximumSecondsSpend) {
        XmlSolverConfigurer configurer = new XmlSolverConfigurer();
        configurer.configure(SOLVER_CONFIG);
        configurer.getConfig().getFinishConfig().setMaximumSecondsSpend(maximumSecondsSpend);
        solver = configurer.buildSolver();
    }

    private void configureDataDir() {
        File dataDir =  new File("data/itc2007/examination/");
        inputDir = new File(dataDir, "input");
        if (!inputDir.exists()) {
            throw new IllegalStateException("The directory inputDir (" + inputDir.getAbsolutePath()
                    + ") does not exist. The working directory should be set to the script directory"
                    + " or to drools-solver-examples.");
        }
        outputDir = new File(dataDir, "output");
        if (!outputDir.exists()) {
            throw new IllegalStateException("The directory outputDir (" + outputDir.getAbsolutePath()
                    + ") does not exist. The working directory should be set to the script directory"
                    + " or to drools-solver-examples.");
        }
    }

    private void processs() {
        List<File> inputFileList = getInputFileList();
        for (File inputFile : inputFileList) {
            Examination examination = (Examination) examinationInputConvertor.readSolution(inputFile);
            solver.setStartingSolution(examination);
            solver.solve();
            examination = (Examination) solver.getBestSolution();
            File outputFile = getOutputFile(inputFile);
            examinationOutputConvertor.writeExamination(examination, outputFile);
        }
    }

    public List<File> getInputFileList() {
        List<File> inputFileList = Arrays.asList(inputDir.listFiles(new ExamInputFileFilter()));
        Collections.sort(inputFileList);
        return inputFileList;
    }

    private File getOutputFile(File inputFile) {
        String outputFileName = inputFile.getName().replaceAll("\\.exam$", ".sln");
        return new File(outputDir, outputFileName);
    }

    public class ExamInputFileFilter implements FileFilter {

        public boolean accept(File file) {
            if (file.isDirectory() || file.isHidden()) {
                return false;
            }
            return file.getName().endsWith(".exam");
        }

    }

}