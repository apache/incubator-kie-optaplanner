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

import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

public final class SolverBuilder {

    private SolverBuilder() {
    }

    public static <Solution_> Solver<Solution_> createSolver(long terminationSeconds, String solverConfigPath) {
        SolverFactory<Solution_> solverFactory = SolverFactory.createFromXmlResource(solverConfigPath);
        List<PhaseConfig> phaseConfigList = solverFactory.getSolverConfig().getPhaseConfigList();

        for (PhaseConfig phaseConfig : phaseConfigList) {
            if (phaseConfig instanceof LocalSearchPhaseConfig) {
                if (phaseConfig.getTerminationConfig() == null) {
                    phaseConfig.setTerminationConfig(new TerminationConfig());
                }

                phaseConfig.getTerminationConfig().setStepCountLimit(1);
                break;
            }
        }

        return solverFactory.buildSolver();
    }
}
