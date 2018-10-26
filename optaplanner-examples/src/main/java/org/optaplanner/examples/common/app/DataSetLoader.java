/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

public final class DataSetLoader {

    private DataSetLoader() {
    }

    public static <Solution_> Solution_ loadUnsolvedProblemFromResource(CommonApp<Solution_> commonApp, String pathToProblemFile) {
        String fileName = CommonApp.class.getResource(pathToProblemFile).getFile();
        return commonApp.createSolutionFileIO().read(new File(fileName));
    }

    public static <Solution_> Solution_ loadUnsolvedProblemFromDataFolder(CommonApp<Solution_> commonApp, String problemFileName) {
        return commonApp.createSolutionFileIO().read(new File(commonApp.determineDataDir(commonApp.getDataDirName()),
                                                              "unsolved" + File.separator + problemFileName));
    }
}
