package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.getListVariableDescriptor;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntityIndependentValueSelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfMoveSelector;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockScoreDirector;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class OriginalListSwapMoveSelectorTest {

    @Test
    void original() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity.createWithValues("A", v2, v1, v4);
        TestdataListEntity.createWithValues("B");
        TestdataListEntity.createWithValues("C", v3);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor = getListVariableDescriptor(scoreDirector);
        OriginalListSwapMoveSelector<TestdataListSolution> moveSelector = new OriginalListSwapMoveSelector<>(
                listVariableDescriptor,
                mockEntityIndependentValueSelector(listVariableDescriptor, v3, v1, v2, v4));

        SolverScope<TestdataListSolution> solverScope = mock(SolverScope.class);
        LocalSearchPhaseScope<TestdataListSolution> phaseScope = mock(LocalSearchPhaseScope.class);
        LocalSearchStepScope<TestdataListSolution> stepScope = mock(LocalSearchStepScope.class);
        when(stepScope.getPhaseScope()).thenReturn(phaseScope);
        when(phaseScope.getSolverScope()).thenReturn(solverScope);
        when(solverScope.<SimpleScore> getScoreDirector()).thenReturn(scoreDirector);

        moveSelector.solvingStarted(solverScope);
        moveSelector.phaseStarted(phaseScope);
        moveSelector.stepStarted(stepScope);

        // Value order: [3, 1, 2, 4]
        // Entity order: [A, B, C]
        // Initial state:
        // - A [2, 1, 4]
        // - B []
        // - C [3]

        assertAllCodesOfMoveSelector(moveSelector,
                "3 {C[0]} <-> 1 {A[1]}",
                "3 {C[0]} <-> 2 {A[0]}",
                "3 {C[0]} <-> 4 {A[2]}",
                "1 {A[1]} <-> 2 {A[0]}",
                "1 {A[1]} <-> 4 {A[2]}",
                "2 {A[0]} <-> 4 {A[2]}");
    }
}
