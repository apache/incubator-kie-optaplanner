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

package org.optaplanner.core.impl.localsearch.decider.acceptor.stepcountinghillclimbing;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.config.localsearch.decider.acceptor.stepcountinghillclimbing.StepCountingHillClimbingType;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptorTest;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

import static org.assertj.core.api.Assertions.assertThat;

public class StepCountingHillClimbingAcceptorTest extends AbstractAcceptorTest {

    @Test
    public void typeStep() {
        StepCountingHillClimbingAcceptor acceptor = new StepCountingHillClimbingAcceptor(2,
                StepCountingHillClimbingType.STEP);

        DefaultSolverScope solverScope = new DefaultSolverScope();
        solverScope.setBestScore(SimpleScore.valueOfInitialized(-1000));
        LocalSearchPhaseScope phaseScope = new LocalSearchPhaseScope(solverScope);
        LocalSearchStepScope lastCompletedStepScope = new LocalSearchStepScope(phaseScope, -1);
        lastCompletedStepScope.setScore(solverScope.getBestScore());
        phaseScope.setLastCompletedStepScope(lastCompletedStepScope);
        acceptor.phaseStarted(phaseScope);

        // thresholdScore = -1000, lastCompletedStepScore = Integer.MIN_VALUE
        LocalSearchStepScope stepScope0 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope0 = buildMoveScope(stepScope0, -500);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope0)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -800))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope0.setStep(moveScope0.getMove());
        stepScope0.setScore(moveScope0.getScore());
        solverScope.setBestScore(moveScope0.getScore());
        acceptor.stepEnded(stepScope0);
        phaseScope.setLastCompletedStepScope(stepScope0);

        // thresholdScore = -1000, lastCompletedStepScore = -500
        LocalSearchStepScope stepScope1 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope1 = buildMoveScope(stepScope1, -700);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope1)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1001))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope1.setStep(moveScope1.getMove());
        stepScope1.setScore(moveScope1.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope1);
        phaseScope.setLastCompletedStepScope(stepScope1);

        // thresholdScore = -700, lastCompletedStepScore = -700
        LocalSearchStepScope stepScope2 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope2 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -700))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -701))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -600))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope2)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -700))).isEqualTo(true); // Repeated call
        stepScope2.setStep(moveScope2.getMove());
        stepScope2.setScore(moveScope2.getScore());
        solverScope.setBestScore(moveScope2.getScore());
        acceptor.stepEnded(stepScope2);
        phaseScope.setLastCompletedStepScope(stepScope2);

        // thresholdScore = -700, lastCompletedStepScore = -400
        LocalSearchStepScope stepScope3 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope3 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -900))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -700))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -701))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope3)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(false); // Repeated call
        stepScope3.setStep(moveScope3.getMove());
        stepScope3.setScore(moveScope3.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope3);
        phaseScope.setLastCompletedStepScope(stepScope3);

        // thresholdScore = -400 (not the best score of -200!), lastCompletedStepScore = -400
        LocalSearchStepScope stepScope4 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope4 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -400))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope4)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -500))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -400))).isEqualTo(true); // Repeated call
        stepScope4.setStep(moveScope4.getMove());
        stepScope4.setScore(moveScope4.getScore());
        solverScope.setBestScore(moveScope4.getScore());
        acceptor.stepEnded(stepScope4);
        phaseScope.setLastCompletedStepScope(stepScope4);

        // thresholdScore = -400, lastCompletedStepScore = -300
        LocalSearchStepScope stepScope5 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope5 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -301))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -400))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope5)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -600))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -301))).isEqualTo(true); // Repeated call
        stepScope5.setStep(moveScope5.getMove());
        stepScope5.setScore(moveScope5.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope5);
        phaseScope.setLastCompletedStepScope(stepScope5);

        acceptor.phaseEnded(phaseScope);
    }

    @Test
    public void typeEqualOrImprovingStep() {
        StepCountingHillClimbingAcceptor acceptor = new StepCountingHillClimbingAcceptor(2,
                StepCountingHillClimbingType.EQUAL_OR_IMPROVING_STEP);

        DefaultSolverScope solverScope = new DefaultSolverScope();
        solverScope.setBestScore(SimpleScore.valueOfInitialized(-1000));
        LocalSearchPhaseScope phaseScope = new LocalSearchPhaseScope(solverScope);
        LocalSearchStepScope lastCompletedStepScope = new LocalSearchStepScope(phaseScope, -1);
        lastCompletedStepScope.setScore(solverScope.getBestScore());
        phaseScope.setLastCompletedStepScope(lastCompletedStepScope);
        acceptor.phaseStarted(phaseScope);

        // thresholdScore = -1000, lastCompletedStepScore = Integer.MIN_VALUE
        LocalSearchStepScope stepScope0 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope0 = buildMoveScope(stepScope0, -500);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope0)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -800))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope0.setStep(moveScope0.getMove());
        stepScope0.setScore(moveScope0.getScore());
        solverScope.setBestScore(moveScope0.getScore());
        acceptor.stepEnded(stepScope0);
        phaseScope.setLastCompletedStepScope(stepScope0);

        // thresholdScore = -1000, lastCompletedStepScore = -500
        LocalSearchStepScope stepScope1 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope1 = buildMoveScope(stepScope1, -700);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope1)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1001))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope1.setStep(moveScope1.getMove());
        stepScope1.setScore(moveScope1.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope1);
        phaseScope.setLastCompletedStepScope(stepScope1);

        // thresholdScore = -1000, lastCompletedStepScore = -700
        LocalSearchStepScope stepScope2 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope2 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -700))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, 1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -1001))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -600))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope2)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -700))).isEqualTo(true); // Repeated call
        stepScope2.setStep(moveScope2.getMove());
        stepScope2.setScore(moveScope2.getScore());
        solverScope.setBestScore(moveScope2.getScore());
        acceptor.stepEnded(stepScope2);
        phaseScope.setLastCompletedStepScope(stepScope2);

        // thresholdScore = -400, lastCompletedStepScore = -400
        LocalSearchStepScope stepScope3 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope3 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -900))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope3)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(false); // Repeated call
        stepScope3.setStep(moveScope3.getMove());
        stepScope3.setScore(moveScope3.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope3);
        phaseScope.setLastCompletedStepScope(stepScope3);

        // thresholdScore = -400, lastCompletedStepScore = -400
        LocalSearchStepScope stepScope4 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope4 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -400))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope4)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -500))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -400))).isEqualTo(true); // Repeated call
        stepScope4.setStep(moveScope4.getMove());
        stepScope4.setScore(moveScope4.getScore());
        solverScope.setBestScore(moveScope4.getScore());
        acceptor.stepEnded(stepScope4);
        phaseScope.setLastCompletedStepScope(stepScope4);

        // thresholdScore = -300, lastCompletedStepScore = -300
        LocalSearchStepScope stepScope5 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope5 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -301))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -400))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope5)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -600))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -301))).isEqualTo(false); // Repeated call
        stepScope5.setStep(moveScope5.getMove());
        stepScope5.setScore(moveScope5.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope5);
        phaseScope.setLastCompletedStepScope(stepScope5);

        acceptor.phaseEnded(phaseScope);
    }

    @Test
    public void typeImprovingStep() {
        StepCountingHillClimbingAcceptor acceptor = new StepCountingHillClimbingAcceptor(2,
                StepCountingHillClimbingType.IMPROVING_STEP);

        DefaultSolverScope solverScope = new DefaultSolverScope();
        solverScope.setBestScore(SimpleScore.valueOfInitialized(-1000));
        LocalSearchPhaseScope phaseScope = new LocalSearchPhaseScope(solverScope);
        LocalSearchStepScope lastCompletedStepScope = new LocalSearchStepScope(phaseScope, -1);
        lastCompletedStepScope.setScore(solverScope.getBestScore());
        phaseScope.setLastCompletedStepScope(lastCompletedStepScope);
        acceptor.phaseStarted(phaseScope);

        // thresholdScore = -1000, lastCompletedStepScore = Integer.MIN_VALUE
        LocalSearchStepScope stepScope0 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope0 = buildMoveScope(stepScope0, -500);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope0)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -800))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope0.setStep(moveScope0.getMove());
        stepScope0.setScore(moveScope0.getScore());
        solverScope.setBestScore(moveScope0.getScore());
        acceptor.stepEnded(stepScope0);
        phaseScope.setLastCompletedStepScope(stepScope0);

        // thresholdScore = -1000, lastCompletedStepScore = -500
        LocalSearchStepScope stepScope1 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope1 = buildMoveScope(stepScope1, -700);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -900))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope1)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope1, -1001))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(true); // Repeated call
        stepScope1.setStep(moveScope1.getMove());
        stepScope1.setScore(moveScope1.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope1);
        phaseScope.setLastCompletedStepScope(stepScope1);

        // thresholdScore = -1000, lastCompletedStepScore = -700
        LocalSearchStepScope stepScope2 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope2 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -700))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, 1000))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -1001))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope2, -600))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope2)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -700))).isEqualTo(true); // Repeated call
        stepScope2.setStep(moveScope2.getMove());
        stepScope2.setScore(moveScope2.getScore());
        solverScope.setBestScore(moveScope2.getScore());
        acceptor.stepEnded(stepScope2);
        phaseScope.setLastCompletedStepScope(stepScope2);

        // thresholdScore = -400, lastCompletedStepScore = -400
        LocalSearchStepScope stepScope3 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope3 = buildMoveScope(stepScope1, -400);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -900))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope3)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope3, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -900))).isEqualTo(false); // Repeated call
        stepScope3.setStep(moveScope3.getMove());
        stepScope3.setScore(moveScope3.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope3);
        phaseScope.setLastCompletedStepScope(stepScope3);

        // thresholdScore = -400, lastCompletedStepScore = -400
        LocalSearchStepScope stepScope4 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope4 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -400))).isEqualTo(true);
        assertThat(acceptor.isAccepted(moveScope4)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -500))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope4, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -400))).isEqualTo(true); // Repeated call
        stepScope4.setStep(moveScope4.getMove());
        stepScope4.setScore(moveScope4.getScore());
        solverScope.setBestScore(moveScope4.getScore());
        acceptor.stepEnded(stepScope4);
        phaseScope.setLastCompletedStepScope(stepScope4);

        // thresholdScore = -400, lastCompletedStepScore = -300
        LocalSearchStepScope stepScope5 = new LocalSearchStepScope(phaseScope);
        LocalSearchMoveScope moveScope5 = buildMoveScope(stepScope1, -300);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -301))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -400))).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -401))).isEqualTo(false);
        assertThat(acceptor.isAccepted(moveScope5)).isEqualTo(true);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -2000))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope5, -600))).isEqualTo(false);
        assertThat(acceptor.isAccepted(buildMoveScope(stepScope0, -301))).isEqualTo(true); // Repeated call
        stepScope5.setStep(moveScope5.getMove());
        stepScope5.setScore(moveScope5.getScore());
        // bestScore unchanged
        acceptor.stepEnded(stepScope5);
        phaseScope.setLastCompletedStepScope(stepScope5);

        acceptor.phaseEnded(phaseScope);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroStepCountingHillClimbingSize() {
        StepCountingHillClimbingAcceptor acceptor = new StepCountingHillClimbingAcceptor(0, StepCountingHillClimbingType.STEP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeStepCountingHillClimbingSize() {
        StepCountingHillClimbingAcceptor acceptor = new StepCountingHillClimbingAcceptor(-1, StepCountingHillClimbingType.STEP);
    }

}
