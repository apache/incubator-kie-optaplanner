/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.common.persistence;

import java.io.*;

public abstract class AbstractTxtSolutionExporter<Solution_> extends AbstractSolutionExporter<Solution_> {

    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = "txt";

    protected AbstractTxtSolutionExporter(SolutionDao<Solution_> solutionDao) {
        super(solutionDao);
    }

    protected AbstractTxtSolutionExporter(boolean withoutDao) {
        super(withoutDao);
    }

    public String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public abstract TxtOutputBuilder<Solution_> createTxtOutputBuilder();

    public void writeSolution(Solution_ solution, File outputFile) {
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            TxtOutputBuilder<Solution_> txtOutputBuilder = createTxtOutputBuilder();
            txtOutputBuilder.setBufferedWriter(writer);
            txtOutputBuilder.setSolution(solution);
            txtOutputBuilder.writeSolution();
            logger.info("Exported: {}", outputFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write the file (" + outputFile.getName() + ").", e);
        }
    }

    public static abstract class TxtOutputBuilder<Solution_> extends OutputBuilder {

        protected BufferedWriter bufferedWriter;

        public void setBufferedWriter(BufferedWriter bufferedWriter) {
            this.bufferedWriter = bufferedWriter;
        }

        public abstract void setSolution(Solution_ solution);

        public abstract void writeSolution() throws IOException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

    }

}
