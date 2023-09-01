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

package org.optaplanner.core.impl.localsearch.decider.forager;

import java.util.List;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.forager.finalist.FinalistPodium;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

/**
 * A {@link LocalSearchForager} which forages accepted moves and ignores unaccepted moves.
 *
 * @see LocalSearchForager
 * @see Acceptor
 */
public class AcceptedLocalSearchForager<Solution_> extends AbstractLocalSearchForager<Solution_> {

    protected final FinalistPodium<Solution_> finalistPodium;
    protected final LocalSearchPickEarlyType pickEarlyType;
    protected final int acceptedCountLimit;
    protected final boolean breakTieRandomly;

    protected long selectedMoveCount;
    protected long acceptedMoveCount;
    protected LocalSearchMoveScope<Solution_> earlyPickedMoveScope;

    public AcceptedLocalSearchForager(FinalistPodium<Solution_> finalistPodium,
            LocalSearchPickEarlyType pickEarlyType, int acceptedCountLimit, boolean breakTieRandomly) {
        this.finalistPodium = finalistPodium;
        this.pickEarlyType = pickEarlyType;
        this.acceptedCountLimit = acceptedCountLimit;
        if (acceptedCountLimit < 1) {
            throw new IllegalArgumentException("The acceptedCountLimit (" + acceptedCountLimit
                    + ") cannot be negative or zero.");
        }
        this.breakTieRandomly = breakTieRandomly;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        finalistPodium.solvingStarted(solverScope);
    }

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        finalistPodium.phaseStarted(phaseScope);
    }

    @Override
    public void stepStarted(LocalSearchStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        finalistPodium.stepStarted(stepScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
        earlyPickedMoveScope = null;
    }

    @Override
    public boolean supportsNeverEndingMoveSelector() {
        // TODO FIXME magical value Integer.MAX_VALUE coming from ForagerConfig
        return acceptedCountLimit < Integer.MAX_VALUE;
    }

    @Override
    public void addMove(LocalSearchMoveScope<Solution_> moveScope) {
        selectedMoveCount++;
        if (moveScope.getAccepted()) {
            acceptedMoveCount++;
            checkPickEarly(moveScope);
        }
        finalistPodium.addMove(moveScope);
    }

    protected void checkPickEarly(LocalSearchMoveScope<Solution_> moveScope) {
        switch (pickEarlyType) {
            case NEVER:
                break;
            case FIRST_BEST_SCORE_IMPROVING:
                Score bestScore = moveScope.getStepScope().getPhaseScope().getBestScore();
                if (moveScope.getScore().compareTo(bestScore) > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            case FIRST_LAST_STEP_SCORE_IMPROVING:
                Score lastStepScore = moveScope.getStepScope().getPhaseScope()
                        .getLastCompletedStepScope().getScore();
                if (moveScope.getScore().compareTo(lastStepScore) > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            default:
                throw new IllegalStateException("The pickEarlyType (" + pickEarlyType + ") is not implemented.");
        }
    }

    @Override
    public boolean isQuitEarly() {
        return earlyPickedMoveScope != null || acceptedMoveCount >= acceptedCountLimit;
    }

    @Override
    public LocalSearchMoveScope<Solution_> pickMove(LocalSearchStepScope<Solution_> stepScope) {
        stepScope.setSelectedMoveCount(selectedMoveCount);
        stepScope.setAcceptedMoveCount(acceptedMoveCount);
        if (earlyPickedMoveScope != null) {
            return earlyPickedMoveScope;
        }
        List<LocalSearchMoveScope<Solution_>> finalistList = finalistPodium.getFinalistList();
        if (finalistList.isEmpty()) {
            return null;
        }
        if (finalistList.size() == 1 || !breakTieRandomly) {
            return finalistList.get(0);
        }
        int randomIndex = stepScope.getWorkingRandom().nextInt(finalistList.size());
        return finalistList.get(randomIndex);
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        finalistPodium.stepEnded(stepScope);
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        finalistPodium.phaseEnded(phaseScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
        earlyPickedMoveScope = null;
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        finalistPodium.solvingEnded(solverScope);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pickEarlyType + ", " + acceptedCountLimit + ")";
    }

}
