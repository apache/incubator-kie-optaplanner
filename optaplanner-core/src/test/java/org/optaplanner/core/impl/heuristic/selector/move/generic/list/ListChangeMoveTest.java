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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class ListChangeMoveTest {

    @Test
    void doMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        ListChangeMove<TestdataListSolution> move = new ListChangeMove<>(variableDescriptor, e1, 1, e2, 1);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1);
        assertThat(e2.getValueList()).containsExactly(v3, v2);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2);
        assertThat(e2.getValueList()).containsExactly(v3);
    }

    @Test
    void isMoveDoable() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // same entity, same index => not doable because the move doesn't change anything
        assertThat(new ListChangeMove<>(variableDescriptor, e1, 1, e1, 1).isMoveDoable(scoreDirector)).isFalse();
        // same entity, different index => doable
        assertThat(new ListChangeMove<>(variableDescriptor, e1, 0, e1, 1).isMoveDoable(scoreDirector)).isTrue();
        // same entity, index == list size => not doable because the element is first removed (list size is reduced by 1)
        assertThat(new ListChangeMove<>(variableDescriptor, e1, 0, e1, 2).isMoveDoable(scoreDirector)).isFalse();
        // different entity => doable
        assertThat(new ListChangeMove<>(variableDescriptor, e1, 0, e2, 0).isMoveDoable(scoreDirector)).isTrue();
    }

    static Stream<Arguments> doAndUndoMoveOnTheSameEntity() {
        // Given E.valueList = [V0, V1, V2, V3, V4],
        // when V2 is moved to destinationIndex (arg0),
        // then the resulting valueList should be arg1.
        return Stream.of(
                arguments(0, asList("V2", "V0", "V1", "V3", "V4")),
                arguments(1, asList("V0", "V2", "V1", "V3", "V4")),
                arguments(2, null), // undoable (no-op)
                arguments(3, asList("V0", "V1", "V3", "V2", "V4")),
                arguments(4, asList("V0", "V1", "V3", "V4", "V2")),
                arguments(5, null) // undoable (out of bounds)
        );
    }

    @ParameterizedTest
    @MethodSource
    void doAndUndoMoveOnTheSameEntity(int destinationIndex, List<String> expectedValueList) {
        // Given...
        final int sourceIndex = 2; // we're always moving V2
        TestdataListValue v0 = new TestdataListValue("V0");
        TestdataListValue v1 = new TestdataListValue("V1");
        TestdataListValue v2 = new TestdataListValue("V2");
        TestdataListValue v3 = new TestdataListValue("V3");
        TestdataListValue v4 = new TestdataListValue("V4");
        TestdataListEntity e = new TestdataListEntity("E", v0, v1, v2, v3, v4);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // When V2 is moved to destinationIndex...
        ListChangeMove<TestdataListSolution> move =
                new ListChangeMove<>(variableDescriptor, e, sourceIndex, e, destinationIndex);

        // Some destinationIndexes make the move undoable.
        if (expectedValueList == null) {
            assertThat(move.isMoveDoable(scoreDirector)).isFalse();
            return;
        }

        // Otherwise, the move is doable...
        assertThat(move.isMoveDoable(scoreDirector)).isTrue();
        // ...and when it's done...
        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);
        // ...V2 ends up at the destinationIndex
        assertThat(e.getValueList().indexOf(v2)).isEqualTo(destinationIndex);
        assertThat(variableDescriptor.getElement(e, destinationIndex)).isEqualTo(v2);
        // ...and the modified value list matches the expectation.
        assertThat(e.getValueList()).map(TestdataObject::toString).isEqualTo(expectedValueList);

        // Making an undo move...
        AbstractMove<TestdataListSolution> undoUndoMove = undoMove.doMove(scoreDirector);
        // ...produces the original move...
        assertThat(undoUndoMove).isEqualTo(move);
        // ...and returns everything to the original state.
        assertThat(e.getValueList().indexOf(v2)).isEqualTo(sourceIndex);
        assertThat(variableDescriptor.getElement(e, sourceIndex)).isEqualTo(v2);
        assertThat(e.getValueList()).containsExactly(v0, v1, v2, v3, v4);
    }

    @Test
    void toStringTest() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        assertThat(new ListChangeMove<>(variableDescriptor, e1, 1, e1, 0)).hasToString("2 {e1[1] -> e1[0]}");
        assertThat(new ListChangeMove<>(variableDescriptor, e1, 0, e2, 1)).hasToString("1 {e1[0] -> e2[1]}");
    }
}
