/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.core.termination;

import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.solver.DefaultSolverScope;

public class PhaseToSolverTerminationBridge extends AbstractTermination {

    private Termination solverTermination;

    public PhaseToSolverTerminationBridge(Termination solverTermination) {
        this.solverTermination = solverTermination;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException(
                "PhaseToSolverTerminationBridge can only be used for phase termination.");
    }

    public boolean isPhaseTerminated(AbstractSolverPhaseScope solverPhaseScope) {
        return solverTermination.isSolverTerminated(solverPhaseScope.getSolverScope());
    }

    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException(
                "PhaseToSolverTerminationBridge can only be used for phase termination.");
    }

    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope solverPhaseScope) {
        return solverTermination.calculateSolverTimeGradient(solverPhaseScope.getSolverScope());
    }

}
