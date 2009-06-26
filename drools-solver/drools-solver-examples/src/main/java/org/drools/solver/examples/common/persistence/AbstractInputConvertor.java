package org.drools.solver.examples.common.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractInputConvertor extends LoggingMain {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = ".txt";
    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = ".xml";

    public void convert() {
        File inputDir = getInputDir();
        File outputDir = getOutputDir();
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
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

    protected File getInputDir() {
        return new File("data/" + getExampleDirName() + "/input/");
    }

    protected File getOutputDir() {
        return new File("data/" + getExampleDirName() + "/unsolved/");
    }

    protected abstract String getExampleDirName();

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public Solution readSolution(File inputFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            return readSolution(bufferedReader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public abstract Solution readSolution(BufferedReader bufferedReader) throws IOException;

    // ************************************************************************
    // Helper methods
    // ************************************************************************

    public void readEmptyLine(BufferedReader bufferedReader) throws IOException {
        readConstantLine(bufferedReader, "");
    }

    public void readConstantLine(BufferedReader bufferedReader, String constantValue) throws IOException {
        String line = bufferedReader.readLine();
        String value = line.trim();
        if (!value.equals(constantValue)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to be a constant value ("
                    + constantValue + ").");
        }
    }

    public int readIntegerValue(BufferedReader bufferedReader) throws IOException {
        return readIntegerValue(bufferedReader, "");
    }

    public int readIntegerValue(BufferedReader bufferedReader, String prefix) throws IOException {
        return readIntegerValue(bufferedReader, prefix, "");
    }

    public int readIntegerValue(BufferedReader bufferedReader, String prefix, String suffix) throws IOException {
        String line = bufferedReader.readLine();
        String value = line.trim();
        if (!value.startsWith(prefix)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to start with prefix ("
                    + prefix + ").");
        }
        value = value.substring(prefix.length());
        if (!value.endsWith(suffix)) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to end with suffix ("
                    + suffix + ").");
        }
        value = value.substring(0, value.length() - suffix.length());
        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Read line (" + line + ") is expected to contain an integer value ("
                    + value + ").", e);
        }
    }

}