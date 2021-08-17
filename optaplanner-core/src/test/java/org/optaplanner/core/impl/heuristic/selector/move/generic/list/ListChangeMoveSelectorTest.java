/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.getListVariableDescriptor;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntityIndependentValueSelector;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntitySelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfMoveSelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfNeverEndingMoveSelector;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

class ListChangeMoveSelectorTest {

    @Test
    void original() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity a = TestdataListEntity.createWithValues("A", v2, v1);
        TestdataListEntity b = TestdataListEntity.createWithValues("B");
        TestdataListEntity c = TestdataListEntity.createWithValues("C", v3);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                PlannerTestUtils.mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());

        ListChangeMoveSelector<TestdataListSolution> moveSelector = new ListChangeMoveSelector<>(
                getListVariableDescriptor(scoreDirector),
                mockEntitySelector(a, b, c),
                mockEntityIndependentValueSelector(v3, v1, v2),
                false);

        SolverScope<TestdataListSolution> solverScope = mock(SolverScope.class);
        when(solverScope.<SimpleScore> getScoreDirector()).thenReturn(scoreDirector);
        moveSelector.solvingStarted(solverScope);

        AbstractPhaseScope<TestdataListSolution> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getWorkingEntityList()).thenReturn(Arrays.asList(a, b, c));
        moveSelector.phaseStarted(phaseScope);

        // Value order: [3, 1, 2]
        // Entity order: [A, B, C]
        // Initial state:
        // - A [2, 1]
        // - B []
        // - C [3]

        assertAllCodesOfMoveSelector(moveSelector,
                // Moving 3 from C[0]
                "3 {C[0]->A[0]}",
                "3 {C[0]->A[1]}",
                "3 {C[0]->A[2]}",
                "3 {C[0]->B[0]}",
                "3 {C[0]->C[0]}", // noop
                "3 {C[0]->C[1]}", // undoable
                // Moving 1 from A[1]
                "1 {A[1]->A[0]}",
                "1 {A[1]->A[1]}", // noop
                "1 {A[1]->A[2]}", // undoable
                "1 {A[1]->B[0]}",
                "1 {A[1]->C[0]}",
                "1 {A[1]->C[1]}",
                // Moving 2 from A[0]
                "2 {A[0]->A[0]}", // noop
                "2 {A[0]->A[1]}",
                "2 {A[0]->A[2]}", // undoable
                "2 {A[0]->B[0]}",
                "2 {A[0]->C[0]}",
                "2 {A[0]->C[1]}");
    }

    @Test
    void random() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity a = new TestdataListEntity("A", v1, v2);
        TestdataListEntity b = new TestdataListEntity("B");
        TestdataListEntity c = new TestdataListEntity("C", v3);

        final int sourceIndexRange = 3; // value count
        final int destinationIndexRange = 6; // value count + entity count

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                PlannerTestUtils.mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());

        Random random = mock(Random.class);
        when(random.nextInt(sourceIndexRange)).thenReturn(1, 0, 2, 2, 2);
        when(random.nextInt(destinationIndexRange)).thenReturn(3, 2, 0, 1, 2);

        ListChangeMoveSelector<TestdataListSolution> moveSelector = new ListChangeMoveSelector<>(
                getListVariableDescriptor(scoreDirector),
                mockEntitySelector(a, b, c),
                mockEntityIndependentValueSelector(v1, v2, v3),
                true);

        SolverScope<TestdataListSolution> solverScope = mock(SolverScope.class);
        when(solverScope.<SimpleScore> getScoreDirector()).thenReturn(scoreDirector);
        when(solverScope.getWorkingRandom()).thenReturn(random);
        moveSelector.solvingStarted(solverScope);

        AbstractPhaseScope<TestdataListSolution> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getWorkingEntityList()).thenReturn(Arrays.asList(a, b, c));
        moveSelector.phaseStarted(phaseScope);

        // Initial state:
        // - A [1, 2]
        // - B []
        // - C [3]

        assertCodesOfNeverEndingMoveSelector(moveSelector,
                "2 {A[1]->B[0]}",
                "1 {A[0]->A[2]}",
                "3 {C[0]->A[0]}",
                "3 {C[0]->A[1]}",
                "3 {C[0]->A[2]}");
    }
}
