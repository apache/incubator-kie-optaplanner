/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.phase;

import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.termination.PhaseToSolverTerminationBridge;
import org.optaplanner.core.impl.solver.termination.Termination;

public abstract class AbstractPhaseFactory<Solution_, Phase_ extends Phase<Solution_>, PhaseConfig_ extends PhaseConfig<PhaseConfig_>> {

    protected final PhaseConfig_ phaseConfig;

    public AbstractPhaseFactory(PhaseConfig_ phaseConfig) {
        this.phaseConfig = phaseConfig;
    }

    public abstract Phase_ buildPhase(int phaseIndex, HeuristicConfigPolicy solverConfigPolicy,
            BestSolutionRecaller<Solution_> bestSolutionRecaller, Termination solverTermination);

    protected Termination buildPhaseTermination(HeuristicConfigPolicy configPolicy, Termination solverTermination) {
        TerminationConfig terminationConfig_ = phaseConfig.getTerminationConfig() == null ? new TerminationConfig()
                : phaseConfig.getTerminationConfig();
        // In case of childThread PART_THREAD, the solverTermination is actually the parent phase's phaseTermination
        // with the bridge removed, so it's ok to add it again
        Termination phaseTermination = new PhaseToSolverTerminationBridge(solverTermination);
        return terminationConfig_.buildTermination(configPolicy, phaseTermination);
    }
}
