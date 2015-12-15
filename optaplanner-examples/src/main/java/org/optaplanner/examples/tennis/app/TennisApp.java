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

package org.optaplanner.examples.tennis.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplanner.examples.tennis.persistence.TennisDao;
import org.optaplanner.examples.tennis.swingui.TennisPanel;

public class TennisApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "org/optaplanner/examples/tennis/solver/tennisSolverConfig.xml";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new TennisApp().init();
    }

    public TennisApp() {
        super("Tennis club scheduling",
                "Assign available spots to teams.\n\n" +
                        "Each team must play an almost equal number of times.\n" +
                        "Each team must play against each other team an almost equal number of times.",
                TennisPanel.LOGO_PATH);
    }

    @Override
    protected Solver createSolver() {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
        return solverFactory.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new TennisPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new TennisDao();
    }

}
