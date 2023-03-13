package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.mockReplayingSubListSelector;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.phaseStarted;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.solvingStarted;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.stepStarted;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntityIndependentValueSelector;
import static org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils.mockEntitySelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfIterator;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockScoreDirector;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.mimic.MimicReplayingSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testutil.TestRandom;

class NearSubListNearbySubListSelectorTest {

    @Test
    void randomSelectionUnrestricted() {
        TestdataListValue v1 = new TestdataListValue("10");
        TestdataListValue v2 = new TestdataListValue("45");
        TestdataListValue v3 = new TestdataListValue("50");
        TestdataListValue v4 = new TestdataListValue("60");
        TestdataListValue v5 = new TestdataListValue("75");
        TestdataListEntity e1 = TestdataListEntity.createWithValues("A", v1, v2, v3, v4);
        TestdataListEntity e2 = TestdataListEntity.createWithValues("B", v5);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());

        // Enumerates all values. Does not affect nearby subList selection.
        EntityIndependentValueSelector<TestdataListSolution> valueSelector =
                mockEntityIndependentValueSelector(scoreDirector, v1, v2, v3, v4, v5);

        // Enumerates all entities. Does not affect nearby subList selection.
        EntitySelector<TestdataListSolution> entitySelector = mockEntitySelector(e1, e2);
        when(entitySelector.getEntityDescriptor()).thenReturn(TestdataListEntity.buildEntityDescriptor());

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor =
                (ListVariableDescriptor<TestdataListSolution>) valueSelector.getVariableDescriptor();

        int minimumSubListSize = 1;
        int maximumSubListSize = Integer.MAX_VALUE;

        // Used to populate the distance matrix with destinations.
        RandomSubListSelector<TestdataListSolution> childSubListSelector = new RandomSubListSelector<>(
                listVariableDescriptor,
                entitySelector,
                valueSelector,
                minimumSubListSize,
                maximumSubListSize);

        // The replaying selector determines the destination matrix origin.
        // In this case, the origin is v5 (because B[0]=v5) in each iteration.
        MimicReplayingSubListSelector<TestdataListSolution> mockReplayingSubListSelector =
                mockReplayingSubListSelector(listVariableDescriptor,
                        subList(e2, 0), // => v5
                        subList(e2, 0),
                        subList(e2, 0));

        NearSubListNearbySubListSelector<TestdataListSolution> nearbySubListSelector =
                new NearSubListNearbySubListSelector<>(childSubListSelector, mockReplayingSubListSelector,
                        new TestDistanceMeter(), new TestNearbyRandom());

        // Each row is consumed by 1 next() call of the RandomSubListNearbySubListIterator.
        // The first number in each row becomes the index of a destination in the nearby matrix.
        // So, in this case, we always select the given origin's (v5) 3rd nearest destination (v2).
        TestRandom testRandom = new TestRandom(
                3, 0,
                3, 1,
                3, 2);

        // A[0]=v1(10)
        // A[1]=v2(45) <= destination
        // A[2]=v3(50)
        // A[3]=v4(60)
        // B[0]=v5(75) <= origin

        SolverScope<TestdataListSolution> solverScope = solvingStarted(nearbySubListSelector, scoreDirector, testRandom);
        AbstractPhaseScope<TestdataListSolution> phaseScopeA = phaseStarted(nearbySubListSelector, solverScope);
        AbstractStepScope<TestdataListSolution> stepScopeA1 = stepStarted(nearbySubListSelector, phaseScopeA);
        // The SubList's assertable code means Entity[fromIndex+subListLength].
        assertAllCodesOfIterator(nearbySubListSelector.iterator(), "A[1+1]", "A[1+2]", "A[1+3]");
        nearbySubListSelector.stepEnded(stepScopeA1);
        nearbySubListSelector.phaseEnded(phaseScopeA);
        nearbySubListSelector.solvingEnded(solverScope);
    }

    static SubList subList(TestdataListEntity entity, int index) {
        return new SubList(entity, index, 1);
    }
}
