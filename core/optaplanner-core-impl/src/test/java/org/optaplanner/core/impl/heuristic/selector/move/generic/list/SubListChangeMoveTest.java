package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class SubListChangeMoveTest {

    @Test
    void isMoveDoable() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // same entity, same index => not doable because the move doesn't change anything
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e1, 1, false).isMoveDoable(scoreDirector)).isFalse();
        // same entity, different index => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e1, 0, false).isMoveDoable(scoreDirector)).isTrue();
        // same entity, index + length <= list size => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 3, e1, 1, false).isMoveDoable(scoreDirector)).isTrue();
        // same entity, index + length > list size => not doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 3, e1, 2, false).isMoveDoable(scoreDirector)).isFalse();
        // different entity => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e2, 0, false).isMoveDoable(scoreDirector)).isTrue();
    }

    @Test
    void doMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListChangeMove<TestdataListSolution> move = new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e2, 0, false);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v4);
        assertThat(e2.getValueList()).containsExactly(v2, v3, v5);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 1, 3);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 1, 1);
        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e2, 0, 0);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e2, 0, 2);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4);
        assertThat(e2.getValueList()).containsExactly(v5);
    }

    @Test
    void doReversingMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListChangeMove<TestdataListSolution> move = new SubListChangeMove<>(variableDescriptor, e1, 0, 3, e2, 1, true);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v4);
        assertThat(e2.getValueList()).containsExactly(v5, v3, v2, v1);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 3);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 0);
        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e2, 1, 1);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e2, 1, 4);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4);
        assertThat(e2.getValueList()).containsExactly(v5);
    }

    @Test
    void doMoveOnSameEntity() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListValue v6 = new TestdataListValue("6");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4, v5, v6);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListChangeMove<TestdataListSolution> move =
                new SubListChangeMove<>(variableDescriptor, e1, 3, 2, e1, 0, false);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v4, v5, v1, v2, v3, v6);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 5);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 5);
        // TODO or this more fine-grained? (Do we allow multiple notifications per entity? (Yes))
        // verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 3, 5);
        // verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 5, 5);
        // verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 0);
        // verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 2);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4, v5, v6);
    }

    @Test
    void tabuIntrospection_twoEntities() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);
        TestdataListEntity e3 = new TestdataListEntity("e3");

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListChangeMove<TestdataListSolution> moveTwoEntities =
                new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e2, 0, false);
        // Do the move first because that might affect the returned values.
        moveTwoEntities.doMoveOnGenuineVariables(scoreDirector);
        assertThat(moveTwoEntities.getPlanningEntities()).containsExactly(e1, e2);
        assertThat(moveTwoEntities.getPlanningValues()).containsExactly(v2, v3, v4);

        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e2, 0, true)).isNotEqualTo(moveTwoEntities);
        //                                                                      ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e2, 1, false)).isNotEqualTo(moveTwoEntities);
        //                                                                   ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e3, 0, false)).isNotEqualTo(moveTwoEntities);
        //                                                               ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e2, 0, false)).isNotEqualTo(moveTwoEntities);
        //                                                            ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 2, 4, e2, 0, false)).isNotEqualTo(moveTwoEntities);
        //                                                         ^  ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e3, 1, 3, e2, 0, false)).isNotEqualTo(moveTwoEntities);
        //                                                     ^
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e2, 0, false)).isEqualTo(moveTwoEntities);
    }

    @Test
    void tabuIntrospection_oneEntity() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListChangeMove<TestdataListSolution> moveOneEntity =
                new SubListChangeMove<>(variableDescriptor, e1, 0, 2, e1, 2, false);
        // Do the move first because that might affect the returned values.
        moveOneEntity.doMoveOnGenuineVariables(scoreDirector);
        assertThat(moveOneEntity.getPlanningEntities()).containsExactly(e1);
        assertThat(moveOneEntity.getPlanningValues()).containsExactly(v1, v2);
    }

    @Test
    void toStringTest() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e1, 0, false)).hasToString("|3| {e1[1..4] -> e1[0]}");
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 1, e2, 1, false)).hasToString("|1| {e1[0..1] -> e2[1]}");
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 1, e2, 1, true))
                .hasToString("|1| {e1[0..1] -reversing-> e2[1]}");
    }
}
