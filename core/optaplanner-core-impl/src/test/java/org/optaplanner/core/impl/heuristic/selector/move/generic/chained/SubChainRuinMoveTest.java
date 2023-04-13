package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockRebasingScoreDirector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChain;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedObject;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedSolution;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

class SubChainRuinMoveTest {

    // do not change
    private static final int anchorIndex = 0;

    // can be adjusted to change the tested chain size
    private static final int lastChainedEntityIndex = 5;

    private static final String moveNotDoableMessage = "the chained ruin move is not doable";

    static private TestdataChainedSolution generateChainedSolution() {
        TestdataChainedSolution initialisedSolution = new TestdataChainedSolution("test solution");
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        List<TestdataChainedAnchor> anchorList = Collections.singletonList(a0);
        initialisedSolution.setChainedAnchorList(anchorList);
        List<TestdataChainedEntity> entityList = new ArrayList<>();
        for (int entityIndex = 1; entityIndex <= lastChainedEntityIndex; ++entityIndex) {
            TestdataChainedEntity testEntity = new TestdataChainedEntity("a".concat(Integer.toString(entityIndex)),
                    entityList.isEmpty() ? a0 : entityList.get(entityIndex - 2));
            testEntity.setUnchainedValue(new TestdataValue()); // some dummy value to fully initialise the solution
            entityList.add(testEntity);
        }
        initialisedSolution.setChainedEntityList(entityList);
        return initialisedSolution;
    }

    static private void checkSubChainRuined(int firstToRuin, int lastToRuin) {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity
                .buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution, SimpleScore> scoreDirector =
                PlannerTestUtils.scoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());

        TestdataChainedSolution solution = generateChainedSolution();
        scoreDirector.setWorkingSolution(solution);
        assertEquals(0, scoreDirector.calculateScore().initScore());

        TestdataChainedEntity[] entities = new TestdataChainedEntity[solution.getChainedEntityList().size()];
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(
                solution.getChainedEntityList().toArray(entities));

        List<TestdataChainedEntity> entitiesToRuin = solution.getChainedEntityList().subList(firstToRuin, lastToRuin);
        List<Object> objectsToRuin = Arrays.asList(entitiesToRuin.toArray());
        SubChainRuinMove<TestdataChainedSolution> move = new SubChainRuinMove<>(
                new SubChain(objectsToRuin),
                variableDescriptor, inverseVariableSupply);
        assertThat((List<Object>) move.getPlanningEntities()).hasSameElementsAs(entitiesToRuin);
        assertEquals(move.getPlanningValues(), Collections.singletonList(null));
        assertThatThrownBy(() -> move.createUndoMove(scoreDirector)).isInstanceOf(UnsupportedOperationException.class);
        assertTrue(move.isMoveDoable(scoreDirector), moveNotDoableMessage);

        move.doMoveOnly(scoreDirector);

        SelectorTestUtils.assertRuined(solution.getChainedEntityList().subList(firstToRuin, lastToRuin));

        List<? extends TestdataChainedObject> chain = Stream
                .concat(Stream.of(solution.getChainedAnchorList().get(anchorIndex)),
                        Stream.concat(solution.getChainedEntityList().subList(anchorIndex, firstToRuin).stream(),
                                solution.getChainedEntityList().subList(lastToRuin, lastChainedEntityIndex).stream()))
                .collect(Collectors.toList());
        SelectorTestUtils.assertChain(chain);
        assertEquals(lastChainedEntityIndex - (lastToRuin - firstToRuin) + 1, chain.size());

        assertEquals(-(lastToRuin - firstToRuin), scoreDirector.calculateScore().initScore());
    }

    @Test
    void ruinTail() {
        int firstToRuin = anchorIndex + 1;

        checkSubChainRuined(firstToRuin, lastChainedEntityIndex);
    }

    @Test
    void ruinMiddle() {
        int firstToRuin = anchorIndex + 1;
        int lastToRuin = lastChainedEntityIndex - 1;

        checkSubChainRuined(firstToRuin, lastToRuin);
    }

    @Test
    void ruinLead() {
        int lastToRuin = anchorIndex + 1;

        checkSubChainRuined(anchorIndex, lastToRuin);
    }

    @Test
    void ruinEntireChain() {
        checkSubChainRuined(anchorIndex, lastChainedEntityIndex);
    }

    @Test
    void ruinNoChain() {
        try {
            checkSubChainRuined(anchorIndex, anchorIndex);
        } catch (AssertionError error) {
            assertTrue(error.getMessage().contains(moveNotDoableMessage));
        }
    }

    @Test
    void rebase() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity
                .buildVariableDescriptorForChainedObject();

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity c1 = new TestdataChainedEntity("c1", null);

        TestdataChainedAnchor destinationA0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity destinationA1 = new TestdataChainedEntity("a1", destinationA0);
        TestdataChainedEntity destinationA2 = new TestdataChainedEntity("a2", destinationA1);
        TestdataChainedEntity destinationA3 = new TestdataChainedEntity("a3", destinationA2);
        TestdataChainedAnchor destinationB0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity destinationC1 = new TestdataChainedEntity("c1", null);

        ScoreDirector<TestdataChainedSolution> destinationScoreDirector = mockRebasingScoreDirector(
                variableDescriptor.getEntityDescriptor().getSolutionDescriptor(), new Object[][] {
                        { a0, destinationA0 },
                        { a1, destinationA1 },
                        { a2, destinationA2 },
                        { a3, destinationA3 },
                        { b0, destinationB0 },
                        { c1, destinationC1 },
                });
        SingletonInverseVariableSupply inverseVariableSupply = mock(SingletonInverseVariableSupply.class);

        assertSameProperties(Arrays.asList(destinationA1, destinationA2),
                new SubChainRuinMove<>(new SubChain(Arrays.asList(a1, a2)), variableDescriptor, inverseVariableSupply)
                        .rebase(destinationScoreDirector));
        assertSameProperties(Arrays.asList(destinationA1, destinationA2, destinationA3),
                new SubChainRuinMove<>(new SubChain(Arrays.asList(a1, a2, a3)), variableDescriptor, inverseVariableSupply)
                        .rebase(destinationScoreDirector));
    }

    private void assertSameProperties(List<Object> entityList, SubChainRuinMove<TestdataChainedSolution> move) {
        assertThat(move.getSubChain().getEntityList()).hasSameElementsAs(entityList);
    }

    @Test
    void toStringTest() {
        TestdataChainedSolution solution = generateChainedSolution();

        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity
                .buildVariableDescriptorForChainedObject();
        TestdataChainedEntity[] entities = new TestdataChainedEntity[solution.getChainedEntityList().size()];
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(
                solution.getChainedEntityList().toArray(entities));

        assertThat(new SubChainRuinMove<>(
                new SubChain(Arrays.asList(solution.getChainedEntityList().toArray())), variableDescriptor,
                inverseVariableSupply).getSimpleMoveTypeDescription())
                .hasToString("SubChainRuinMove(TestdataChainedEntity.chainedObject)");

        assertThat(new SubChainRuinMove<>(
                new SubChain(Arrays.asList(solution.getChainedEntityList().toArray())), variableDescriptor,
                inverseVariableSupply))
                .hasToString("[a1..a5] {[a1..a5] -> null}");

        assertThat(new SubChainRuinMove<>(
                new SubChain(Arrays.asList(
                        solution.getChainedEntityList().subList(anchorIndex + 1, lastChainedEntityIndex - 1).toArray())),
                variableDescriptor, inverseVariableSupply))
                .hasToString("[a2..a4] {[a2..a4] -> null}");
    }
}
