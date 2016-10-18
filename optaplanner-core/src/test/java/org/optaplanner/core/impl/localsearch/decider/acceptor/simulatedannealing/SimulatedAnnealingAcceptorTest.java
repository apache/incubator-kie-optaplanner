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

package org.optaplanner.core.impl.localsearch.decider.acceptor.simulatedannealing;

import java.util.Random;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptorTest;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SimulatedAnnealingAcceptorTest extends AbstractAcceptorTest {

    @Test
    public void lateAcceptanceSize() {
        SimulatedAnnealingAcceptor acceptor = new SimulatedAnnealingAcceptor();
        acceptor.setStartingTemperature(SimpleScore.valueOfInitialized(200));

        DefaultSolverScope solverScope = new DefaultSolverScope();
        solverScope.setBestScore(SimpleScore.valueOfInitialized(-1000));
        Random workingRandom = mock(Random.class);
        solverScope.setWorkingRandom(workingRandom);
        LocalSearchPhaseScope phaseScope = new LocalSearchPhaseScope(solverScope);
        LocalSearchStepScope lastCompletedStepScope = new LocalSearchStepScope(phaseScope, -1);
        lastCompletedStepScope.setScore(SimpleScore.valueOfInitialized(-1000));
        phaseScope.setLastCompletedStepScope(lastCompletedStepScope);
        acceptor.phaseStarted(phaseScope);

        LocalSearchStepScope stepScope0 = new LocalSearchStepScope(phaseScope);
        stepScope0.setTimeGradient(0.0);
        acceptor.stepStarted(stepScope0);
        LocalSearchMoveScope moveScope0 = buildMoveScope(stepScope0, -500);
        when(workingRandom.nextDouble()).thenReturn(0.3);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1300))).isFalse();
        when(workingRandom.nextDouble()).thenReturn(0.3);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1200))).isTrue();
        when(workingRandom.nextDouble()).thenReturn(0.4);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1200))).isFalse();
        assertThat(acceptor.isAccepted(moveScope0)).isTrue();
        stepScope0.setStep(moveScope0.getMove());
        stepScope0.setScore(moveScope0.getScore());
        solverScope.setBestScore(moveScope0.getScore());
        acceptor.stepEnded(stepScope0);
        phaseScope.setLastCompletedStepScope(stepScope0);

        LocalSearchStepScope stepScope1 = new LocalSearchStepScope(phaseScope);
        stepScope1.setTimeGradient(0.5);
        acceptor.stepStarted(stepScope1);
        LocalSearchMoveScope moveScope1 = buildMoveScope(stepScope1, -800);
        when(workingRandom.nextDouble()).thenReturn(0.13);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -700))).isTrue();
        when(workingRandom.nextDouble()).thenReturn(0.14);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -700))).isFalse();
        when(workingRandom.nextDouble()).thenReturn(0.04);
        assertThat(acceptor.isAccepted(moveScope1)).isTrue();
        stepScope1.setStep(moveScope1.getMove());
        stepScope1.setScore(moveScope1.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope1);
        phaseScope.setLastCompletedStepScope(stepScope1);

        LocalSearchStepScope stepScope2 = new LocalSearchStepScope(phaseScope);
        stepScope2.setTimeGradient(1.0);
        acceptor.stepStarted(stepScope2);
        LocalSearchMoveScope moveScope2 = buildMoveScope(stepScope1, -400);
        when(workingRandom.nextDouble()).thenReturn(0.01);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -800))).isTrue();
        when(workingRandom.nextDouble()).thenReturn(0.01);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -801))).isFalse();
        when(workingRandom.nextDouble()).thenReturn(0.01);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -1200))).isFalse();
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -700))).isTrue();
        assertThat(acceptor.isAccepted(moveScope2)).isTrue();
        stepScope2.setStep(moveScope2.getMove());
        stepScope2.setScore(moveScope2.getScore());
        solverScope.setBestScore(moveScope2.getScore());
        acceptor.stepEnded(stepScope2);
        phaseScope.setLastCompletedStepScope(stepScope2);

        acceptor.phaseEnded(phaseScope);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSimulatedAnnealingSize() {
        SimulatedAnnealingAcceptor acceptor = new SimulatedAnnealingAcceptor();
        acceptor.setStartingTemperature(HardMediumSoftScore.parseScore("1, -1, 2"));
        acceptor.phaseStarted(null);
    }

}
