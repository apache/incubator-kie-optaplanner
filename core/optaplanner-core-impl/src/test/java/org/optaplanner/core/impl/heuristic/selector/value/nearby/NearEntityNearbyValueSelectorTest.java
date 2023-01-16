package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfValueSelectorForEntity;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.verifyPhaseLifecycle;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.support.VariableListenerSupport;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.ManualEntityMimicRecorder;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedObject;

class NearEntityNearbyValueSelectorTest {

    @Test
    void originalSelection() {
        final TestdataEntity africa = new TestdataEntity("Africa");
        final TestdataEntity europe = new TestdataEntity("Europe");
        final TestdataEntity oceania = new TestdataEntity("Oceania");
        final TestdataValue morocco = new TestdataValue("Morocco");
        final TestdataValue spain = new TestdataValue("Spain");
        final TestdataValue australia = new TestdataValue("Australia");
        final TestdataValue brazil = new TestdataValue("Brazil");

        GenuineVariableDescriptor variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        EntityIndependentValueSelector childValueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                morocco, spain, australia, brazil);
        NearbyDistanceMeter<TestdataEntity, TestdataValue> meter = (origin, destination) -> {
            if (origin == africa) {
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
            } else if (origin == europe) {
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
            } else if (origin == oceania) {
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
            } else {
                throw new IllegalStateException("The origin (" + origin + ") is not implemented.");
            }
        };
        EntitySelector entitySelector = SelectorTestUtils.mockEntitySelector(variableDescriptor.getEntityDescriptor(), africa,
                europe, oceania);
        ManualEntityMimicRecorder entityMimicRecorder = new ManualEntityMimicRecorder(entitySelector);
        NearEntityNearbyValueSelector valueSelector = new NearEntityNearbyValueSelector(
                childValueSelector, new MimicReplayingEntitySelector(entityMimicRecorder), meter, null, false);

        SolverScope<TestdataSolution> solverScope = mockSolverScope();
        valueSelector.solvingStarted(solverScope);

        // The movingEntity can be the same (ChangeMove) or different (SwapMove) as the nearby source
        TestdataEntity movingEntity = europe;

        AbstractPhaseScope<TestdataSolution> phaseScopeA = mockPhaseScope(solverScope);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA1);
        entityMimicRecorder.setRecordedEntity(europe);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Spain", "Morocco", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeA1);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA2);
        entityMimicRecorder.setRecordedEntity(africa);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Morocco", "Spain", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeA2);

        valueSelector.phaseEnded(phaseScopeA);

        AbstractPhaseScope<TestdataSolution> phaseScopeB = mockPhaseScope(solverScope);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB1);
        entityMimicRecorder.setRecordedEntity(africa);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Morocco", "Spain", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeB1);

        AbstractStepScope stepScopeB2 = mock(AbstractStepScope.class);
        when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB2);
        entityMimicRecorder.setRecordedEntity(oceania);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Australia", "Brazil", "Morocco", "Spain");
        valueSelector.stepEnded(stepScopeB2);

        AbstractStepScope stepScopeB3 = mock(AbstractStepScope.class);
        when(stepScopeB3.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB3);
        entityMimicRecorder.setRecordedEntity(europe);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Spain", "Morocco", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeB3);

        valueSelector.phaseEnded(phaseScopeB);

        valueSelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childValueSelector, 1, 2, 5);
    }

    private SolverScope<TestdataSolution> mockSolverScope() {
        SolutionDescriptor<TestdataSolution> solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        doReturn(solutionDescriptor).when(scoreDirector).getSolutionDescriptor();
        doReturn(VariableListenerSupport.create(scoreDirector)).when(scoreDirector).getSupplyManager();

        SolverScope<TestdataSolution> solverScope = mock(SolverScope.class);
        doReturn(scoreDirector).when(solverScope).getScoreDirector();
        return solverScope;
    }

    private AbstractPhaseScope<TestdataSolution> mockPhaseScope(SolverScope<TestdataSolution> solverScope) {
        return spy(new AbstractPhaseScope<>(solverScope) {
            @Override
            public AbstractStepScope<TestdataSolution> getLastCompletedStepScope() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Test
    void originalSelectionChained() {
        final TestdataChainedEntity morocco = new TestdataChainedEntity("Morocco");
        final TestdataChainedEntity spain = new TestdataChainedEntity("Spain");
        final TestdataChainedEntity australia = new TestdataChainedEntity("Australia");
        final TestdataChainedAnchor brazil = new TestdataChainedAnchor("Brazil");

        GenuineVariableDescriptor variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        EntityIndependentValueSelector childValueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                morocco, spain, australia, brazil);
        NearbyDistanceMeter<TestdataChainedEntity, TestdataChainedObject> meter = (origin, destination) -> {
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
            } else {
                throw new IllegalStateException("The origin (" + origin + ") is not implemented.");
            }
        };
        EntitySelector entitySelector = SelectorTestUtils.mockEntitySelector(variableDescriptor.getEntityDescriptor(), morocco,
                spain, australia);
        ManualEntityMimicRecorder entityMimicRecorder = new ManualEntityMimicRecorder(entitySelector);
        NearEntityNearbyValueSelector valueSelector = new NearEntityNearbyValueSelector(
                childValueSelector, new MimicReplayingEntitySelector(entityMimicRecorder), meter, null, false);

        SolverScope<TestdataSolution> solverScope = mockSolverScope();
        valueSelector.solvingStarted(solverScope);

        // The movingEntity can be the same (ChangeMove) or different (SwapMove) as the nearby source
        TestdataChainedEntity movingEntity = spain;

        AbstractPhaseScope<TestdataSolution> phaseScopeA = mockPhaseScope(solverScope);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA1);
        entityMimicRecorder.setRecordedEntity(spain);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Morocco", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeA1);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA2);
        entityMimicRecorder.setRecordedEntity(morocco);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Spain", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeA2);

        valueSelector.phaseEnded(phaseScopeA);

        AbstractPhaseScope<TestdataSolution> phaseScopeB = mockPhaseScope(solverScope);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB1);
        entityMimicRecorder.setRecordedEntity(morocco);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Spain", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeB1);

        AbstractStepScope stepScopeB2 = mock(AbstractStepScope.class);
        when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB2);
        entityMimicRecorder.setRecordedEntity(australia);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Brazil", "Morocco", "Spain");
        valueSelector.stepEnded(stepScopeB2);

        AbstractStepScope stepScopeB3 = mock(AbstractStepScope.class);
        when(stepScopeB3.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB3);
        entityMimicRecorder.setRecordedEntity(spain);
        assertAllCodesOfValueSelectorForEntity(valueSelector, movingEntity, "Morocco", "Brazil", "Australia");
        valueSelector.stepEnded(stepScopeB3);

        valueSelector.phaseEnded(phaseScopeB);

        valueSelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childValueSelector, 1, 2, 5);
    }

}
