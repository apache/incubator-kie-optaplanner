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
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.solver.DefaultSolverScope;

public class UnimprovedStepCountTermination extends AbstractTermination {

    private int maximumUnimprovedStepCount = 100;

    public void setMaximumUnimprovedStepCount(int maximumUnimprovedStepCount) {
        this.maximumUnimprovedStepCount = maximumUnimprovedStepCount;
        if (maximumUnimprovedStepCount < 0) {
            throw new IllegalArgumentException("Property maximumUnimprovedStepCount (" + maximumUnimprovedStepCount
                    + ") must be greater or equal to 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException("StepCountTermination can only be used for phase termination.");
    }

    public boolean isPhaseTerminated(AbstractSolverPhaseScope solverPhaseScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(solverPhaseScope);
        return unimprovedStepCount >= maximumUnimprovedStepCount;
    }

    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException("StepCountTermination can only be used for phase termination.");
    }

    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope solverPhaseScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(solverPhaseScope);
        double timeGradient = ((double) unimprovedStepCount) / ((double) maximumUnimprovedStepCount);
        return Math.min(timeGradient, 1.0);
    }

    private int calculateUnimprovedStepCount(AbstractSolverPhaseScope solverPhaseScope) {
        int bestStepIndex = solverPhaseScope.getBestSolutionStepIndex();
        int lastStepIndex = solverPhaseScope.getLastCompletedStepScope().getStepIndex();
        return lastStepIndex - bestStepIndex;
    }

}
