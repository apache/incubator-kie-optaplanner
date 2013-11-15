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

package org.optaplanner.core.impl.termination;

import org.optaplanner.core.impl.phase.AbstractSolverPhaseScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class UnimprovedTimeMillisSpendTermination extends AbstractTermination {

    private long maximumUnimprovedTimeMillisSpendTermination;

    public void setMaximumUnimprovedTimeMillisSpendTermination(long maximumUnimprovedTimeMillisSpendTermination) {
        this.maximumUnimprovedTimeMillisSpendTermination = maximumUnimprovedTimeMillisSpendTermination;
        if (maximumUnimprovedTimeMillisSpendTermination <= 0L) {
            throw new IllegalArgumentException("Property maximumUnimprovedTimeMillisSpendTermination (" + maximumUnimprovedTimeMillisSpendTermination
                    + ") must be greater than 0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        return isTerminated(calculateTimeSpendSinceBestSolution(solverScope));
    }

    public boolean isPhaseTerminated(AbstractSolverPhaseScope phaseScope) {
        return isSolverTerminated(phaseScope.getSolverScope());
    }

    private boolean isTerminated(long timeMillisSpend) {
        return timeMillisSpend >= maximumUnimprovedTimeMillisSpendTermination;
    }

    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        long timeMillisSpendSinceBestSolution = calculateTimeSpendSinceBestSolution(solverScope);
        return calculateTimeGradient(timeMillisSpendSinceBestSolution);
    }

    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope phaseScope) {
        long timeMillisSpendSinceBestSolution = calculateTimeSpendSinceBestSolution(phaseScope.getSolverScope());
        return calculateTimeGradient(timeMillisSpendSinceBestSolution);
    }

    private double calculateTimeGradient(long timeMillisSpend) {
        double timeGradient = ((double) timeMillisSpend) / ((double) maximumUnimprovedTimeMillisSpendTermination);
        return Math.min(timeGradient, 1.0);
    }

    protected long calculateTimeSpendSinceBestSolution(DefaultSolverScope solverScope) {
        long timeSpend = -1;
        long now = System.currentTimeMillis();
        long bestSolverTimeMillis = solverScope.getBestScoreSystemTimeMillis();

        if (0L > bestSolverTimeMillis) {
            timeSpend = (now - bestSolverTimeMillis);
        } else {
            timeSpend = (now - solverScope.getStartingSystemTimeMillis());
        }
        return timeSpend;
    }

}
