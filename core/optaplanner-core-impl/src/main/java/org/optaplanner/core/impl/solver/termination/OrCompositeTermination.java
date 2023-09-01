/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.solver.termination;

import java.util.List;

import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.thread.ChildThreadType;

public class OrCompositeTermination<Solution_> extends AbstractCompositeTermination<Solution_> {

    public OrCompositeTermination(List<Termination<Solution_>> terminationList) {
        super(terminationList);
    }

    public OrCompositeTermination(Termination<Solution_>... terminations) {
        super(terminations);
    }

    // ************************************************************************
    // Terminated methods
    // ************************************************************************

    /**
     * @param solverScope never null
     * @return true if any of the Termination is terminated.
     */
    @Override
    public boolean isSolverTerminated(SolverScope<Solution_> solverScope) {
        for (Termination<Solution_> termination : terminationList) {
            if (termination.isSolverTerminated(solverScope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param phaseScope never null
     * @return true if any of the Termination is terminated.
     */
    @Override
    public boolean isPhaseTerminated(AbstractPhaseScope<Solution_> phaseScope) {
        for (Termination<Solution_> termination : terminationList) {
            if (termination.isPhaseTerminated(phaseScope)) {
                return true;
            }
        }
        return false;
    }

    // ************************************************************************
    // Time gradient methods
    // ************************************************************************

    /**
     * Calculates the maximum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored.
     *
     * @param solverScope never null
     * @return the maximum timeGradient of the Terminations.
     */
    @Override
    public double calculateSolverTimeGradient(SolverScope<Solution_> solverScope) {
        double timeGradient = 0.0;
        for (Termination<Solution_> termination : terminationList) {
            double nextTimeGradient = termination.calculateSolverTimeGradient(solverScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

    /**
     * Calculates the maximum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored.
     *
     * @param phaseScope never null
     * @return the maximum timeGradient of the Terminations.
     */
    @Override
    public double calculatePhaseTimeGradient(AbstractPhaseScope<Solution_> phaseScope) {
        double timeGradient = 0.0;
        for (Termination<Solution_> termination : terminationList) {
            double nextTimeGradient = termination.calculatePhaseTimeGradient(phaseScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

    // ************************************************************************
    // Other methods
    // ************************************************************************

    @Override
    public OrCompositeTermination<Solution_> createChildThreadTermination(SolverScope<Solution_> solverScope,
            ChildThreadType childThreadType) {
        return new OrCompositeTermination<>(createChildThreadTerminationList(solverScope, childThreadType));
    }

    @Override
    public String toString() {
        return "Or(" + terminationList + ")";
    }

}
