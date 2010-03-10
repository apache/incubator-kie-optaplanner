package org.drools.planner.examples.common.persistence;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractTxtOutputConverter extends AbstractOutputConverter {

    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = ".txt";

    protected AbstractTxtOutputConverter(SolutionDao solutionDao) {
        super(solutionDao);
    }

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public abstract TxtOutputBuilder createTxtOutputBuilder();

    public void writeSolution(Solution solution, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            TxtOutputBuilder txtOutputBuilder = createTxtOutputBuilder();
            txtOutputBuilder.setBufferedWriter(bufferedWriter);
            txtOutputBuilder.setSolution(solution);
            txtOutputBuilder.writeSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    public abstract class TxtOutputBuilder {

        protected BufferedWriter bufferedWriter;

        public void setBufferedWriter(BufferedWriter bufferedWriter) {
            this.bufferedWriter = bufferedWriter;
        }
        
        public abstract void setSolution(Solution solution);

        public abstract void writeSolution() throws IOException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

    }

}
