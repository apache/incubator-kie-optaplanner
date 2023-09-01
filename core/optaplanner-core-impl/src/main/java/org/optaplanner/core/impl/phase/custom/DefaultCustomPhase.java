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

package org.optaplanner.core.impl.phase.custom;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.phase.AbstractPhase;
import org.optaplanner.core.impl.phase.custom.scope.CustomPhaseScope;
import org.optaplanner.core.impl.phase.custom.scope.CustomStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * Default implementation of {@link CustomPhase}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
final class DefaultCustomPhase<Solution_> extends AbstractPhase<Solution_> implements CustomPhase<Solution_> {

    private final List<CustomPhaseCommand<Solution_>> customPhaseCommandList;

    private DefaultCustomPhase(Builder<Solution_> builder) {
        super(builder);
        customPhaseCommandList = builder.customPhaseCommandList;
    }

    @Override
    public String getPhaseTypeString() {
        return "Custom";
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solve(SolverScope<Solution_> solverScope) {
        CustomPhaseScope<Solution_> phaseScope = new CustomPhaseScope<>(solverScope);
        phaseStarted(phaseScope);
        for (CustomPhaseCommand<Solution_> customPhaseCommand : customPhaseCommandList) {
            solverScope.checkYielding();
            if (phaseTermination.isPhaseTerminated(phaseScope)) {
                break;
            }
            CustomStepScope<Solution_> stepScope = new CustomStepScope<>(phaseScope);
            stepStarted(stepScope);
            doStep(stepScope, customPhaseCommand);
            stepEnded(stepScope);
            phaseScope.setLastCompletedStepScope(stepScope);
        }
        phaseEnded(phaseScope);
    }

    private void doStep(CustomStepScope<Solution_> stepScope, CustomPhaseCommand<Solution_> customPhaseCommand) {
        InnerScoreDirector<Solution_, ?> scoreDirector = stepScope.getScoreDirector();
        customPhaseCommand.changeWorkingSolution(scoreDirector);
        calculateWorkingStepScore(stepScope, customPhaseCommand);
        solver.getBestSolutionRecaller().processWorkingSolutionDuringStep(stepScope);
    }

    public void stepEnded(CustomStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        CustomPhaseScope<Solution_> phaseScope = stepScope.getPhaseScope();
        if (logger.isDebugEnabled()) {
            logger.debug("{}    Custom step ({}), time spent ({}), score ({}), {} best score ({}).",
                    logIndentation,
                    stepScope.getStepIndex(),
                    phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                    stepScope.getScore(),
                    stepScope.getBestScoreImproved() ? "new" : "   ",
                    phaseScope.getBestScore());
        }
    }

    public void phaseEnded(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        phaseScope.endingNow();
        logger.info("{}Custom phase ({}) ended: time spent ({}), best score ({}),"
                + " score calculation speed ({}/sec), step total ({}).",
                logIndentation,
                phaseIndex,
                phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                phaseScope.getBestScore(),
                phaseScope.getPhaseScoreCalculationSpeed(),
                phaseScope.getNextStepIndex());
    }

    public static final class Builder<Solution_> extends AbstractPhase.Builder<Solution_> {

        private final List<CustomPhaseCommand<Solution_>> customPhaseCommandList;

        public Builder(int phaseIndex, String logIndentation, Termination<Solution_> phaseTermination,
                List<CustomPhaseCommand<Solution_>> customPhaseCommandList) {
            super(phaseIndex, logIndentation, phaseTermination);
            this.customPhaseCommandList = List.copyOf(customPhaseCommandList);
        }

        @Override
        public DefaultCustomPhase<Solution_> build() {
            return new DefaultCustomPhase<>(this);
        }
    }
}
