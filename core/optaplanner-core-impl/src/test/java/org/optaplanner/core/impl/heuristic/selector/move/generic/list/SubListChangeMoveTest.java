package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e1, 1).isMoveDoable(scoreDirector)).isFalse();
        // same entity, different index => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e1, 0).isMoveDoable(scoreDirector)).isTrue();
        // same entity, index + length <= list size => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 3, e1, 1).isMoveDoable(scoreDirector)).isTrue();
        // same entity, index + length > list size => not doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 3, e1, 2).isMoveDoable(scoreDirector)).isFalse();
        // different entity => doable
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e2, 0).isMoveDoable(scoreDirector)).isTrue();
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

        SubListChangeMove<TestdataListSolution> move = new SubListChangeMove<>(variableDescriptor, e1, 1, 2, e2, 0);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v4);
        assertThat(e2.getValueList()).containsExactly(v2, v3, v5);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4);
        assertThat(e2.getValueList()).containsExactly(v5);
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

        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 1, 3, e1, 0)).hasToString("|3| {e1[1..4] -> e1[0]}");
        assertThat(new SubListChangeMove<>(variableDescriptor, e1, 0, 1, e2, 1)).hasToString("|1| {e1[0..1] -> e2[1]}");
    }
}
