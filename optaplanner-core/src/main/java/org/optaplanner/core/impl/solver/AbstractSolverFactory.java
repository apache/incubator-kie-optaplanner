/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

public class AbstractSolverFactory extends SolverFactory {

    protected final ClassLoader classLoader;

    protected SolverConfig solverConfig = null;

    public AbstractSolverFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public SolverConfig getSolverConfig() {
        if (solverConfig == null) {
            throw new IllegalStateException("The solverConfig (" + solverConfig + ") is null," +
                    " call configure(...) first.");
        }
        return solverConfig;
    }

    public Solver buildSolver() {
        if (solverConfig == null) {
            throw new IllegalStateException("The solverConfig (" + solverConfig + ") is null," +
                    " call configure(...) first.");
        }
        return solverConfig.buildSolver(classLoader);
    }

    @Override
    public SolverFactory cloneSolverFactory() {
        if (solverConfig == null) {
            throw new IllegalStateException("The solverConfig (" + solverConfig + ") is null," +
                    " call configure(...) first.");
        }
        SolverConfig solverConfigClone = new SolverConfig(solverConfig);
        return new EmptySolverFactory(classLoader, solverConfigClone);
    }

}
