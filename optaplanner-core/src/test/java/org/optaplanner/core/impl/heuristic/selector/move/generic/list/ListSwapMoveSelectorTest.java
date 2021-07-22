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
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfMoveSelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfNeverEndingMoveSelector;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class ListSwapMoveSelectorTest {

    @Test
    void original() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity a = new TestdataListEntity("A", v1, v2);
        TestdataListEntity b = new TestdataListEntity("B");
        TestdataListEntity c = new TestdataListEntity("C", v3);
        EntitySelector<TestdataListSolution> leftEntitySelector = SelectorTestUtils.mockEntitySelector(
                TestdataListEntity.class, a, b, c);
        EntitySelector<TestdataListSolution> rightEntitySelector = SelectorTestUtils.mockEntitySelector(
                TestdataListEntity.class, a, b, c);

        ListSwapMoveSelector<TestdataListSolution> moveSelector = new ListSwapMoveSelector<>(
                TestdataListEntity.buildVariableDescriptorForValueList(),
                leftEntitySelector,
                rightEntitySelector,
                false);

        AbstractPhaseScope<TestdataListSolution> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getWorkingEntityList()).thenReturn(Arrays.asList(a, b, c));
        moveSelector.phaseStarted(phaseScope);

        assertAllCodesOfMoveSelector(moveSelector,
                // moving A[0]
                "A[0]<->A[0]", // undoable
                "A[0]<->A[1]",
                "A[0]<->C[0]",
                // moving A[1]
                "A[1]<->A[0]", // redundant?
                "A[1]<->A[1]", // undoable
                "A[1]<->C[0]",
                // B has no elements, so no moves
                // moving C[0]
                "C[0]<->A[0]", // redundant?
                "C[0]<->A[1]", // redundant?
                "C[0]<->C[0]" // undoable
        );
    }

    @Test
    void random() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity a = new TestdataListEntity("A", v1, v2);
        TestdataListEntity b = new TestdataListEntity("B");
        TestdataListEntity c = new TestdataListEntity("C", v3);
        EntitySelector<TestdataListSolution> leftEntitySelector = SelectorTestUtils.mockEntitySelector(
                TestdataListEntity.class, a, b, c);
        EntitySelector<TestdataListSolution> rightEntitySelector = SelectorTestUtils.mockEntitySelector(
                TestdataListEntity.class, a, b, c);

        ListSwapMoveSelector<TestdataListSolution> moveSelector = new ListSwapMoveSelector<>(
                TestdataListEntity.buildVariableDescriptorForValueList(),
                leftEntitySelector,
                rightEntitySelector,
                true);

        Random random = mock(Random.class);
        when(random.nextInt(3)).thenReturn(1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2, 2);

        SolverScope<TestdataListSolution> solverScope = mock(SolverScope.class);
        when(solverScope.getWorkingRandom()).thenReturn(random);
        moveSelector.solvingStarted(solverScope);

        AbstractPhaseScope<TestdataListSolution> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getWorkingEntityList()).thenReturn(Arrays.asList(a, b, c));
        moveSelector.phaseStarted(phaseScope);

        assertCodesOfNeverEndingMoveSelector(moveSelector,
                "A[1]<->A[1]",
                "C[0]<->C[0]",
                "A[1]<->A[1]",
                "C[0]<->C[0]",
                "A[1]<->A[1]",
                "C[0]<->C[0]");
    }
}
