/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.cheaptime.persistence;

import java.io.File;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class CheapTimeSolutionFileIO implements SolutionFileIO {

    public static final String OUTPUT_FILE_EXTENSION = "txt";

    private CheapTimeImporter importer = new CheapTimeImporter();
    private CheapTimeExporter exporter = new CheapTimeExporter();

    public String getInputFileExtension() {
        return null;
    }

    public String getOutputFileExtension() {
        return OUTPUT_FILE_EXTENSION;
    }

    public Solution read(File inputSolutionFile) {
        return importer.readSolution(inputSolutionFile);
    }

    public void write(Solution solution, File outputSolutionFile) {
        exporter.writeSolution(solution, outputSolutionFile);
    }

}
