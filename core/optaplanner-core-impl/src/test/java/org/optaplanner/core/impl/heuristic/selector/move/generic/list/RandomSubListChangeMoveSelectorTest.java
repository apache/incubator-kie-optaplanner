package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.getListVariableDescriptor;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntityIndependentValueSelector;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntitySelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfNeverEndingMoveSelector;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;
import org.optaplanner.core.impl.testutil.TestRandom;

class RandomSubListChangeMoveSelectorTest {

    @Test
    void random() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity a = TestdataListEntity.createWithValues("A", v1, v2, v3, v4);
        TestdataListEntity b = TestdataListEntity.createWithValues("B");

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                PlannerTestUtils.mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());

        RandomSubListChangeMoveSelector<TestdataListSolution> moveSelector = new RandomSubListChangeMoveSelector<>(
                getListVariableDescriptor(scoreDirector),
                mockEntitySelector(a, b),
                // The value selector is longer than the number of expected codes because it is expected
                // to be never ending, so it must not be exhausted after the last asserted code.
                mockEntityIndependentValueSelector(v1, v1, v1, v1, v1, v1, v1, v1, v1, v1, v1));

        final int destinationIndexRange = 6; // value count + entity count
        final int b0 = destinationIndexRange - 1; // value count + entity count
        // Alternating subList and destination indexes.
        TestRandom random = new TestRandom(0, b0, 1, b0, 2, b0, 3, b0, 4, b0, 5, b0, 6, b0, 7, b0, 8, b0, 9, b0, 99, 99);

        SolverScope<TestdataListSolution> solverScope = mock(SolverScope.class);
        when(solverScope.<SimpleScore> getScoreDirector()).thenReturn(scoreDirector);
        when(solverScope.getWorkingRandom()).thenReturn(random);
        moveSelector.solvingStarted(solverScope);

        // Every possible subList is selected.
        assertCodesOfNeverEndingMoveSelector(moveSelector,
                "|4| {A[0..4]->B[0]}",
                "|3| {A[0..3]->B[0]}",
                "|3| {A[1..4]->B[0]}",
                "|2| {A[0..2]->B[0]}",
                "|2| {A[1..3]->B[0]}",
                "|2| {A[2..4]->B[0]}",
                "|1| {A[0..1]->B[0]}",
                "|1| {A[1..2]->B[0]}",
                "|1| {A[2..3]->B[0]}",
                "|1| {A[3..4]->B[0]}");

        random.assertIntBoundJustRequested(destinationIndexRange);
    }
}
