/**
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

package org.drools.planner.core.localsearch.decider.deciderscorecomparator;

import junit.framework.TestCase;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.score.DefaultHardAndSoftScore;
import org.drools.planner.core.score.comparator.NaturalScoreComparator;
import org.drools.planner.core.score.definition.HardAndSoftScoreDefinition;

/**
 * @author Geoffrey De Smet
 */
public class ShiftingHardPenaltyDeciderScoreComparatorFactoryTest extends TestCase {

    public void testShiftingPenaltyActiveAndHardWeight() {
        // Setup
        ShiftingHardPenaltyDeciderScoreComparatorFactory deciderScoreComparatorFactory
                = new ShiftingHardPenaltyDeciderScoreComparatorFactory();
        deciderScoreComparatorFactory.setHardScoreActivationThreshold(-10);
        deciderScoreComparatorFactory.setSuccessiveNoHardChangeMinimum(1);
        deciderScoreComparatorFactory.setSuccessiveNoHardChangeMaximum(3);
        deciderScoreComparatorFactory.setSuccessiveNoHardChangeRepetitionMultiplicand(5.0);
        deciderScoreComparatorFactory.setHardWeightSurvivalRatio(0.9);

        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        deciderScoreComparatorFactory.solvingStarted(localSearchSolverScope);
        StepScope stepScope = localSearchSolverScope.getLastCompletedStepScope();
        // Under hardScoreActivationThreshold 1
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertTrue(deciderScoreComparatorFactory.createDeciderScoreComparator() instanceof NaturalScoreComparator);
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-11, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Under hardScoreActivationThreshold 2
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertTrue(deciderScoreComparatorFactory.createDeciderScoreComparator() instanceof NaturalScoreComparator);
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        localSearchSolverScope.setBestSolutionStepIndex(stepScope.getStepIndex());
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 0
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertTrue(deciderScoreComparatorFactory.createDeciderScoreComparator() instanceof NaturalScoreComparator);
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 1
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertEquals(1000, ((HardPenaltyDeciderScoreComparator)
                deciderScoreComparatorFactory.createDeciderScoreComparator()).getHardWeight());
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 2
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertEquals(900, ((HardPenaltyDeciderScoreComparator)
                deciderScoreComparatorFactory.createDeciderScoreComparator()).getHardWeight());
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 3
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertEquals(810, ((HardPenaltyDeciderScoreComparator)
                deciderScoreComparatorFactory.createDeciderScoreComparator()).getHardWeight());
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 4
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertTrue(deciderScoreComparatorFactory.createDeciderScoreComparator() instanceof NaturalScoreComparator);
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
        // Above hardScoreActivationThreshold 5
        stepScope = nextStepScope(stepScope);
        deciderScoreComparatorFactory.beforeDeciding(stepScope);
        assertEquals(1000, ((HardPenaltyDeciderScoreComparator)
                deciderScoreComparatorFactory.createDeciderScoreComparator()).getHardWeight());
        deciderScoreComparatorFactory.stepDecided(stepScope);
        stepScope.setScore(DefaultHardAndSoftScore.valueOf(-10, -200));
        deciderScoreComparatorFactory.stepTaken(stepScope);
    }

    private StepScope nextStepScope(StepScope lastStepScope) {
        StepScope stepScope = new StepScope(lastStepScope.getLocalSearchSolverScope());
        lastStepScope.getLocalSearchSolverScope().setLastCompletedStepScope(lastStepScope);
        stepScope.setStepIndex(lastStepScope.getStepIndex() + 1);
        return stepScope;
    }

    private LocalSearchSolverScope createLocalSearchSolverScope() {
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setScoreDefinition(new HardAndSoftScoreDefinition());
        localSearchSolverScope.setBestScore(DefaultHardAndSoftScore.valueOf(-11, -200));
        localSearchSolverScope.setBestSolutionStepIndex(1000);
        StepScope lastStepScope = new StepScope(localSearchSolverScope);
        lastStepScope.setStepIndex(1000);
        lastStepScope.setScore(DefaultHardAndSoftScore.valueOf(-11, -200));
        localSearchSolverScope.setLastCompletedStepScope(lastStepScope);
        return localSearchSolverScope;
    }

}