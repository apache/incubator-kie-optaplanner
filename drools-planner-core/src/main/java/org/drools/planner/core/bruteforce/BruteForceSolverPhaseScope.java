/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.bruteforce;

import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.solver.DefaultSolverScope;

public class BruteForceSolverPhaseScope extends AbstractSolverPhaseScope {

    private BruteForceStepScope lastCompletedBruteForceStepScope;

    public BruteForceSolverPhaseScope(DefaultSolverScope solverScope) {
        super(solverScope);
    }

    public AbstractStepScope getLastCompletedStepScope() {
        return lastCompletedBruteForceStepScope;
    }

    public BruteForceStepScope getLastCompletedBruteForceStepScope() {
        return lastCompletedBruteForceStepScope;
    }

    public void setLastCompletedBruteForceStepScope(BruteForceStepScope lastCompletedBruteForceStepScope) {
        this.lastCompletedBruteForceStepScope = lastCompletedBruteForceStepScope;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

}
