package org.optaplanner.core.impl.heuristic.selector.entity.nearby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.mockEntitySelector;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.mockReplayingEntitySelector;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.phaseStarted;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.solvingStarted;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.stepStarted;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfNeverEndingOfEntitySelector;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.verifyPhaseLifecycle;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockScoreDirector;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testutil.TestNearbyRandom;
import org.optaplanner.core.impl.testutil.TestRandom;

class NearEntityNearbyEntitySelectorTest {
    private final TestdataEntity morocco = new TestdataEntity("Morocco");
    private final TestdataEntity spain = new TestdataEntity("Spain");
    private final TestdataEntity australia = new TestdataEntity("Australia");
    private final TestdataEntity brazil = new TestdataEntity("Brazil");

    private NearEntityNearbyEntitySelector<TestdataSolution> getEntitySelector(
            EntitySelector<TestdataSolution> childEntitySelector,
            MimicReplayingEntitySelector<TestdataSolution> mimicReplayingEntitySelector, boolean randomSelection) {
        NearbyDistanceMeter<TestdataEntity, TestdataEntity> meter = (origin, destination) -> {
            if (origin == morocco) {
                if (destination == morocco) {
                    return 0.0;
                } else if (destination == spain) {
                    return 1.0;
                } else if (destination == australia) {
                    return 100.0;
                } else if (destination == brazil) {
                    return 50.0;
                } else {
                    throw new IllegalStateException("The destination (" + destination + ") is not implemented.");
                }
            } else if (origin == spain) {
                if (destination == morocco) {
                    return 1.0;
                } else if (destination == spain) {
                    return 0.0;
                } else if (destination == australia) {
                    return 101.0;
                } else if (destination == brazil) {
                    return 51.0;
                } else {
                    throw new IllegalStateException("The destination (" + destination + ") is not implemented.");
                }
            } else if (origin == australia) {
                if (destination == morocco) {
                    return 100.0;
                } else if (destination == spain) {
                    return 101.0;
                } else if (destination == australia) {
                    return 0.0;
                } else if (destination == brazil) {
                    return 60.0;
                } else {
                    throw new IllegalStateException("The destination (" + destination + ") is not implemented.");
                }
            } else if (origin == brazil) {
                if (destination == morocco) {
                    return 55.0;
                } else if (destination == spain) {
                    return 53.0;
                } else if (destination == australia) {
                    return 61.0;
                } else if (destination == brazil) {
                    return 0.0;
                } else {
                    throw new IllegalStateException("The destination (" + destination + ") is not implemented.");
                }
            } else {
                throw new IllegalStateException("The origin (" + origin + ") is not implemented.");
            }
        };

        return new NearEntityNearbyEntitySelector<>(
                childEntitySelector, mimicReplayingEntitySelector, meter, new TestNearbyRandom(), randomSelection);
    }

    private void verifyIterator(NearEntityNearbyEntitySelector<TestdataSolution> entitySelector) {
        Iterator<?> entityIterator = entitySelector.iterator();

        assertTrue(entityIterator.hasNext());
        TestdataEntity firstEntity = (TestdataEntity) entityIterator.next();
        assertEquals(spain, firstEntity);

        assertTrue(entityIterator.hasNext());
        TestdataEntity secondEntity = (TestdataEntity) entityIterator.next();
        assertEquals(brazil, secondEntity);

        assertTrue(entityIterator.hasNext());
        TestdataEntity thirdEntity = (TestdataEntity) entityIterator.next();
        assertEquals(australia, thirdEntity);
    }

    @Test
    void randomSelection() {
        EntitySelector<TestdataSolution> childEntitySelector = mockEntitySelector(TestdataEntity.buildEntityDescriptor(),
                morocco, spain, australia, brazil);
        MimicReplayingEntitySelector<TestdataSolution> mimicReplayingEntitySelector =
                // The last entity () is not used, it just makes the selector appear never ending.
                mockReplayingEntitySelector(TestdataEntity.buildEntityDescriptor(), morocco, spain, australia, brazil, morocco);
        NearEntityNearbyEntitySelector<TestdataSolution> entitySelector =
                getEntitySelector(childEntitySelector, mimicReplayingEntitySelector, true);

        TestRandom workingRandom = new TestRandom(0, 1, 2, 0);

        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataSolution.buildSolutionDescriptor());
        SolverScope<TestdataSolution> solverScope = solvingStarted(entitySelector, scoreDirector, workingRandom);
        AbstractPhaseScope<TestdataSolution> phaseScopeA = phaseStarted(entitySelector, solverScope);
        AbstractStepScope<TestdataSolution> stepScopeA1 = stepStarted(entitySelector, phaseScopeA);
        assertCodesOfNeverEndingOfEntitySelector(entitySelector, childEntitySelector.getSize() - 1,
                "Spain", "Brazil", "Spain", "Spain");
        entitySelector.stepEnded(stepScopeA1);
        entitySelector.phaseEnded(phaseScopeA);
        entitySelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childEntitySelector, 1, 1, 1);
    }

    @Test
    void multipleRandomSelection() {
        EntitySelector<TestdataSolution> childEntitySelector = mockEntitySelector(TestdataEntity.buildEntityDescriptor(),
                morocco, spain, australia, brazil);
        MimicReplayingEntitySelector<TestdataSolution> mimicReplayingEntitySelector =
                // finite iterator.
                mockReplayingEntitySelector(TestdataEntity.buildEntityDescriptor(), morocco);
        NearEntityNearbyEntitySelector<TestdataSolution> entitySelector =
                getEntitySelector(childEntitySelector, mimicReplayingEntitySelector, true);

        TestRandom workingRandom = new TestRandom(0, 1, 2, 0);

        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataSolution.buildSolutionDescriptor());
        solvingStarted(entitySelector, scoreDirector, workingRandom);

        verifyIterator(entitySelector);
    }

    @Test
    void multipleOriginalSelection() {
        EntitySelector<TestdataSolution> childEntitySelector = mockEntitySelector(TestdataEntity.buildEntityDescriptor(),
                morocco, spain, australia, brazil);
        MimicReplayingEntitySelector<TestdataSolution> mimicReplayingEntitySelector =
                // finite iterator.
                mockReplayingEntitySelector(TestdataEntity.buildEntityDescriptor(), morocco);
        NearEntityNearbyEntitySelector<TestdataSolution> entitySelector =
                getEntitySelector(childEntitySelector, mimicReplayingEntitySelector, false);

        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataSolution.buildSolutionDescriptor());
        solvingStarted(entitySelector, scoreDirector);

        verifyIterator(entitySelector);
    }
}
