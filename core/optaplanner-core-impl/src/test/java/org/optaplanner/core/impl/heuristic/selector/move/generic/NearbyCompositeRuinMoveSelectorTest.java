package org.optaplanner.core.impl.heuristic.selector.move.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.mockEntitySelector;
import static org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils.solvingStarted;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfNeverEndingMoveSelector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListUtils;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testdata.domain.shadow.inverserelation.TestdataInverseRelationEntity;
import org.optaplanner.core.impl.testdata.domain.shadow.inverserelation.TestdataInverseRelationSolution;
import org.optaplanner.core.impl.testdata.domain.shadow.inverserelation.TestdataInverseRelationValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

class NearbyCompositeRuinMoveSelectorTest {

    @Test
    void listRuin() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity a = TestdataListEntity.createWithValues("A", v2, v1);
        TestdataListEntity b = TestdataListEntity.createWithValues("B", v3);
        TestdataListEntity c = TestdataListEntity.createWithValues("C", v4);

        EntitySelector<TestdataListSolution> leftEntitySelector = TestdataListUtils.mockEntitySelector(a);
        when(leftEntitySelector.getSize()).thenReturn(3L);
        EntitySelector<TestdataListSolution> rightEntitySelector = TestdataListUtils.mockEntitySelector(b, c);

        NearbyCompositeRuinMoveSelector<TestdataListSolution> moveSelector =
                new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                        leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                        leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80);

        try {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "1 {A[1] -> null}+2 {A[0] -> null}+3 {B[0] -> null}");
        } catch (AssertionError error) {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "3 {B[0] -> null}+1 {A[1] -> null}+2 {A[0] -> null}");
        }
    }

    @Test
    void genuineRuin() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataEntity a = new TestdataEntity("A", v1);
        TestdataEntity b = new TestdataEntity("B", v2);
        TestdataEntity c = new TestdataEntity("C", v3);

        EntitySelector<TestdataSolution> leftEntitySelector = mockEntitySelector(TestdataEntity.buildEntityDescriptor(), a);
        when(leftEntitySelector.getSize()).thenReturn(3L);
        EntitySelector<TestdataSolution> rightEntitySelector = mockEntitySelector(TestdataEntity.buildEntityDescriptor(), b, c);

        NearbyCompositeRuinMoveSelector<TestdataSolution> moveSelector =
                new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                        leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                        leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80);

        try {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "A->null+B->null");
        } catch (AssertionError error) {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "B->null+A->null");
        }
    }

    @Test
    void inverseRelationRuin() {
        SolutionDescriptor<TestdataInverseRelationSolution> solutionDescriptor =
                TestdataInverseRelationSolution.buildSolutionDescriptor();
        EntityDescriptor<TestdataInverseRelationSolution> shadowEntityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataInverseRelationValue.class);

        TestdataInverseRelationValue val1 = new TestdataInverseRelationValue("1");
        TestdataInverseRelationValue val2 = new TestdataInverseRelationValue("2");
        TestdataInverseRelationValue val3 = new TestdataInverseRelationValue("3");
        TestdataInverseRelationEntity a = new TestdataInverseRelationEntity("a", val1);
        TestdataInverseRelationEntity b = new TestdataInverseRelationEntity("b", val2);
        TestdataInverseRelationEntity c = new TestdataInverseRelationEntity("c", val3);
        TestdataInverseRelationEntity d = new TestdataInverseRelationEntity("d", val3);

        EntitySelector<TestdataInverseRelationSolution> leftEntitySelector =
                mockEntitySelector(shadowEntityDescriptor, val1);
        when(leftEntitySelector.getSize()).thenReturn(3L);
        EntitySelector<TestdataInverseRelationSolution> rightEntitySelector =
                mockEntitySelector(shadowEntityDescriptor, val2, val3);

        NearbyCompositeRuinMoveSelector<TestdataInverseRelationSolution> moveSelector =
                new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                        leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                        leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80);

        try {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "[a]->null+[b]->null");
        } catch (AssertionError error) {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "[b]->null+[a]->null");
        }
    }

    @Test
    void inverseRelationListRuin() {
        SolutionDescriptor<TestdataListSolution> solutionDescriptor =
                TestdataListSolution.buildSolutionDescriptor();
        EntityDescriptor<TestdataListSolution> shadowEntityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataListValue.class);

        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity a = TestdataListEntity.createWithValues("A", v2, v1);
        TestdataListEntity b = TestdataListEntity.createWithValues("B", v3);
        TestdataListEntity c = TestdataListEntity.createWithValues("C", v4);

        EntitySelector<TestdataListSolution> leftEntitySelector = mockEntitySelector(shadowEntityDescriptor, v1);
        when(leftEntitySelector.getSize()).thenReturn(3L);
        EntitySelector<TestdataListSolution> rightEntitySelector = mockEntitySelector(shadowEntityDescriptor, v3, v4);

        NearbyCompositeRuinMoveSelector<TestdataListSolution> moveSelector =
                new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                        leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                        leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80);

        try {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "1 {A[1] -> null}+3 {B[0] -> null}");
        } catch (AssertionError error) {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "3 {B[0] -> null}+1 {A[1] -> null}");
        }
    }

    @Test
    void chainRuin() {
        EntityDescriptor<TestdataChainedSolution> entityDescriptor = TestdataChainedEntity
                .buildEntityDescriptor();
        InnerScoreDirector<TestdataChainedSolution, SimpleScore> scoreDirector =
                PlannerTestUtils.mockScoreDirector(entityDescriptor.getSolutionDescriptor());

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);

        TestdataChainedSolution initialisedSolution = new TestdataChainedSolution("test solution");
        List<TestdataChainedAnchor> anchorList = Collections.singletonList(a0);
        initialisedSolution.setChainedAnchorList(anchorList);
        List<TestdataChainedEntity> entityList = Arrays.asList(a1, a2, a3);
        initialisedSolution.setChainedEntityList(entityList);
        scoreDirector.setWorkingSolution(initialisedSolution);

        EntitySelector<TestdataChainedSolution> leftEntitySelector = mockEntitySelector(entityDescriptor, a1);
        when(leftEntitySelector.getSize()).thenReturn(3L);
        EntitySelector<TestdataChainedSolution> rightEntitySelector = mockEntitySelector(entityDescriptor, a2, a3);

        NearbyCompositeRuinMoveSelector<TestdataChainedSolution> moveSelector =
                new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                        leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                        leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80);

        solvingStarted(moveSelector, scoreDirector);

        try {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "[a1..a2] {[a1..a2] -> null}+a2->null+a1->null");
        } catch (AssertionError error) {
            assertCodesOfNeverEndingMoveSelector(moveSelector,
                    "[a1..a2] {[a1..a2] -> null}+a1->null+a2->null");
        }
    }

    @Test
    void toStringTest() {
        EntitySelector<TestdataListSolution> leftEntitySelector = TestdataListUtils.mockEntitySelector();
        EntitySelector<TestdataListSolution> rightEntitySelector = TestdataListUtils.mockEntitySelector();

        assertThat(new NearbyCompositeRuinMoveSelector<>(leftEntitySelector, rightEntitySelector,
                leftEntitySelector.getEntityDescriptor().getGenuineVariableDescriptorList(),
                leftEntitySelector.getEntityDescriptor().getShadowVariableDescriptors(), 80).toString()).matches(
                        "NearbyCompositeRuinMoveSelector\\(Mock for EntitySelector, hashCode: [0-9]+, Mock for EntitySelector, hashCode: [0-9]+\\)");
    }
}
