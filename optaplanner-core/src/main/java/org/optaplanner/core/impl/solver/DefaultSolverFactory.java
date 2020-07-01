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

package org.optaplanner.core.impl.solver;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see SolverFactory
 */
public final class DefaultSolverFactory<Solution_> extends SolverFactory<Solution_> {

    private final SolverConfig solverConfig;

    public DefaultSolverFactory(SolverConfig solverConfig) {
        if (solverConfig == null) {
            throw new IllegalStateException("The solverConfig (" + solverConfig + ") cannot be null.");
        }
        this.solverConfig = solverConfig;
    }

    @Override
    public Solver<Solution_> buildSolver() {
        return solverConfig.buildSolver();
    }

    public InnerScoreDirectorFactory<Solution_> getScoreDirectorFactory() {
        return solverConfig.buildScoreDirectorFactory(solverConfig.determineEnvironmentMode());
    }

}
