package org.drools.planner.examples.common.persistence;

import java.io.File;
import java.util.Arrays;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.app.LoggingMain;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractSolutionExporter extends LoggingMain {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = ".xml";
    protected SolutionDao solutionDao;

    public AbstractSolutionExporter(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
    }

    protected File getInputDir() {
        return new File(solutionDao.getDataDir(), "solved");
    }

    protected File getOutputDir() {
        return new File(solutionDao.getDataDir(), "output");
    }

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    protected abstract String getOutputFileSuffix();

    public void convertAll() {
        File inputDir = getInputDir();
        File outputDir = getOutputDir();
        File[] inputFiles = inputDir.listFiles();
        Arrays.sort(inputFiles);
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-planner-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(getInputFileSuffix())) {
                Solution solution = solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0,
                        inputFileName.length() - getInputFileSuffix().length())
                        + getOutputFileSuffix();
                File outputFile = new File(outputDir, outputFileName);
                writeSolution(solution, outputFile);
            }
        }
    }

    public abstract void writeSolution(Solution solution, File outputFile);
}
