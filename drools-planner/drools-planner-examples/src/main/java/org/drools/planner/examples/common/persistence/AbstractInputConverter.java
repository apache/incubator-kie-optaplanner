package org.drools.planner.examples.common.persistence;

import java.io.File;
import java.util.Arrays;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.app.LoggingMain;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractInputConverter extends LoggingMain {

    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = ".xml";
    
    protected SolutionDao solutionDao;

    public AbstractInputConverter(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
    }

    protected File getInputDir() {
        return new File(solutionDao.getDataDir(), "input");
    }

    protected abstract String getInputFileSuffix();

    protected File getOutputDir() {
        return new File(solutionDao.getDataDir(), "unsolved");
    }

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public void convertAll() {
        File inputDir = getInputDir();
        File outputDir = getOutputDir();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-planner-examples and contain: " + inputDir);
        }
        Arrays.sort(inputFiles);
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(getInputFileSuffix())) {
                Solution solution = readSolution(inputFile);
                String outputFileName = inputFileName.substring(0,
                        inputFileName.length() - getInputFileSuffix().length())
                        + getOutputFileSuffix();
                File outputFile = new File(outputDir, outputFileName);
                solutionDao.writeSolution(solution, outputFile);
            }
        }
    }

    public abstract Solution readSolution(File inputFile);

}
