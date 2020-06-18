/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.entity.decorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCode;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.verifyPhaseLifecycle;

import java.util.Iterator;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

public class ProbabilityEntitySelectorTest {

    @Test
    public void randomSelection() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class,
                new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"), new TestdataEntity("e4"));

        SelectionProbabilityWeightFactory<TestdataSolution, TestdataEntity> probabilityWeightFactory = (scoreDirector,
                entity) -> {
            switch (entity.getCode()) {
                case "e1":
                    return 1000.0;
                case "e2":
                    return 200.0;
                case "e3":
                    return 30.0;
                case "e4":
                    return 4.0;
                default:
                    throw new IllegalStateException("Unknown entity (" + entity + ").");
            }
        };
        EntitySelector entitySelector = new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.STEP,
                probabilityWeightFactory);

        Random workingRandom = mock(Random.class);
        when(workingRandom.nextDouble()).thenReturn(1222.0 / 1234.0, 111.0 / 1234.0, 0.0, 1230.0 / 1234.0, 1199.0 / 1234.0);

        SolverScope solverScope = mock(SolverScope.class);
        when(solverScope.getWorkingRandom()).thenReturn(workingRandom);
        entitySelector.solvingStarted(solverScope);
        AbstractPhaseScope phaseScopeA = mock(AbstractPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        when(phaseScopeA.getWorkingRandom()).thenReturn(workingRandom);
        entitySelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        when(stepScopeA1.getWorkingRandom()).thenReturn(workingRandom);
        entitySelector.stepStarted(stepScopeA1);

        assertThat(entitySelector.isCountable()).isTrue();
        assertThat(entitySelector.isNeverEnding()).isTrue();
        assertThat(entitySelector.getSize()).isEqualTo(4L);
        Iterator<Object> iterator = entitySelector.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertCode("e3", iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertCode("e1", iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertCode("e1", iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertCode("e4", iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertCode("e2", iterator.next());
        assertThat(iterator.hasNext()).isTrue();

        entitySelector.stepEnded(stepScopeA1);
        entitySelector.phaseEnded(phaseScopeA);
        entitySelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childEntitySelector, 1, 1, 1);
        verify(childEntitySelector, times(1)).iterator();
    }

    @Test
    public void isCountable() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        EntitySelector entitySelector = new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.STEP, null);
        assertThat(entitySelector.isCountable()).isTrue();
    }

    @Test
    public void isNeverEnding() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        EntitySelector entitySelector = new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.STEP, null);
        assertThat(entitySelector.isNeverEnding()).isTrue();
    }

    @Test
    public void getSize() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class,
                new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"), new TestdataEntity("e4"));
        SelectionProbabilityWeightFactory<TestdataSolution, TestdataEntity> probabilityWeightFactory = (scoreDirector,
                entity) -> {
            switch (entity.getCode()) {
                case "e1":
                    return 1000.0;
                case "e2":
                    return 200.0;
                case "e3":
                    return 30.0;
                case "e4":
                    return 4.0;
                default:
                    throw new IllegalStateException("Unknown entity (" + entity + ").");
            }
        };
        ProbabilityEntitySelector entitySelector = new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.STEP,
                probabilityWeightFactory);
        entitySelector.constructCache(mock(SolverScope.class));
        assertThat(entitySelector.getSize()).isEqualTo(4);
    }

    @Test
    public void withNeverEndingSelection() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        when(childEntitySelector.isNeverEnding()).thenReturn(true);
        SelectionProbabilityWeightFactory prob = mock(SelectionProbabilityWeightFactory.class);
        assertThatIllegalStateException().isThrownBy(
                () -> new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.STEP, prob));
    }

    @Test
    public void withoutCachedSelectionType() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        SelectionProbabilityWeightFactory prob = mock(SelectionProbabilityWeightFactory.class);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new ProbabilityEntitySelector(childEntitySelector, SelectionCacheType.JUST_IN_TIME, prob));
    }

}
