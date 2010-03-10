package org.drools.planner.examples.common.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractTxtInputConverter extends AbstractInputConverter {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = ".txt";

    protected AbstractTxtInputConverter(SolutionDao solutionDao) {
        super(solutionDao);
    }

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    public abstract TxtInputBuilder createTxtInputBuilder();

    public Solution readSolution(File inputFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            TxtInputBuilder txtInputBuilder = createTxtInputBuilder();
            txtInputBuilder.setBufferedReader(bufferedReader);
            return txtInputBuilder.readSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public abstract class TxtInputBuilder {

        protected BufferedReader bufferedReader;

        public void setBufferedReader(BufferedReader bufferedReader) {
            this.bufferedReader = bufferedReader;
        }

        public abstract Solution readSolution() throws IOException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

        public void readEmptyLine() throws IOException {
            readConstantLine("");
        }

        public void readConstantLine(String constantValue) throws IOException {
            String line = bufferedReader.readLine();
            String value = line.trim();
            if (!value.equals(constantValue)) {
                throw new IllegalArgumentException("Read line (" + line + ") is expected to be a constant value ("
                        + constantValue + ").");
            }
        }

        public int readIntegerValue() throws IOException {
            return readIntegerValue("");
        }

        public int readIntegerValue(String prefix) throws IOException {
            return readIntegerValue(prefix, "");
        }

        public int readIntegerValue(String prefix, String suffix) throws IOException {
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

}
