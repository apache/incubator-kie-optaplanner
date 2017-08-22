/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.common.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.examples.common.business.ProblemFileComparator;
import org.optaplanner.examples.common.persistence.SolutionDao;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class UnsolvedDirSolveAllTurtleTest<Solution_> extends SolveAllTurtleTest<Solution_> {

    protected static <Solution_> Collection<Object[]> getUnsolvedDirFilesAsParameters(SolutionDao<Solution_> solutionDao) {
        List<Object[]> filesAsParameters = new ArrayList<>();
        File dataDir = solutionDao.getDataDir();
        File unsolvedDataDir = new File(dataDir, "unsolved");
        if (!unsolvedDataDir.exists()) {
            throw new IllegalStateException("The directory unsolvedDataDir (" + unsolvedDataDir.getAbsolutePath()
                    + ") does not exist.");
        } else {
            List<File> fileList = new ArrayList<>(
                    FileUtils.listFiles(unsolvedDataDir, new String[]{solutionDao.getFileExtension()}, true));
            Collections.sort(fileList, new ProblemFileComparator());
            for (File file : fileList) {
                filesAsParameters.add(new Object[]{file});
            }
        }
        return filesAsParameters;
    }

    protected File dataFile;
    protected SolutionDao<Solution_> solutionDao;

    protected UnsolvedDirSolveAllTurtleTest(File dataFile) {
        this.dataFile = dataFile;
    }

    @Before
    public void setUp() {
        solutionDao = createSolutionDao();
    }

    protected abstract SolutionDao<Solution_> createSolutionDao();

    @Override
    protected Solution_ readProblem() {
        return solutionDao.readSolution(dataFile);
    }

}
