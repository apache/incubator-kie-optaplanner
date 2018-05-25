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

package org.optaplanner.examples.rocktour.app;

import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.rocktour.domain.RockTourSolution;
import org.optaplanner.examples.rocktour.persistence.RockTourXlsxFileIO;
import org.optaplanner.examples.rocktour.swingui.RockTourPanel;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class RockTourApp extends CommonApp<RockTourSolution> {

    public static final String SOLVER_CONFIG
            = "org/optaplanner/examples/rocktour/solver/rockTourSolverConfig.xml";

    public static final String DATA_DIR_NAME = "rocktour";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new RockTourApp().init();
    }

    public RockTourApp() {
        super("Rock tour",
                "Plan the most profitable and ecological rock tour.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                RockTourPanel.LOGO_PATH);
    }

    @Override
    protected RockTourPanel createSolutionPanel() {
        return new RockTourPanel();
    }

    @Override
    public SolutionFileIO<RockTourSolution> createSolutionFileIO() {
        return new RockTourXlsxFileIO();
    }

}
