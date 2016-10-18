/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfArray;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockScoreDirector;

public class CompositeMoveTest {

    @Test
    public void createUndoMove() {
        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        Move a = new DummyMove("a");
        Move b = new DummyMove("b");
        Move c = new DummyMove("c");
        CompositeMove move = new CompositeMove(a, b, c);
        CompositeMove undoMove = move.createUndoMove(scoreDirector);
        assertAllCodesOfArray(move.getMoves(), "a", "b", "c");
        assertAllCodesOfArray(undoMove.getMoves(), "undo c", "undo b", "undo a");
    }

    @Test
    public void doMove() {
        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        Move a = mock(Move.class);
        Move b = mock(Move.class);
        Move c = mock(Move.class);
        CompositeMove move = new CompositeMove(a, b, c);
        move.doMove(scoreDirector);
        verify(a, times(1)).doMove(scoreDirector);
        verify(b, times(1)).doMove(scoreDirector);
        verify(c, times(1)).doMove(scoreDirector);
    }

    @Test
    public void buildEmptyMove() {
        assertThat(CompositeMove.buildMove(new ArrayList<>())).isInstanceOf(NoChangeMove.class);
        assertThat(CompositeMove.buildMove()).isInstanceOf(NoChangeMove.class);
    }

    @Test
    public void buildOneElemMove() {
        Move tmpMove = new DummyMove();
        Move move = CompositeMove.buildMove(Collections.singletonList(tmpMove));
        assertThat(move).isInstanceOf(DummyMove.class);

        move = CompositeMove.buildMove(tmpMove);
        assertThat(move).isInstanceOf(DummyMove.class);
    }

    @Test
    public void buildTwoElemMove() {
        Move first = new DummyMove();
        Move second = new NoChangeMove();
        Move move = CompositeMove.buildMove(Arrays.asList(first, second));
        assertThat(move).isInstanceOf(CompositeMove.class);
        assertThat(((CompositeMove) move).getMoves()[0]).isInstanceOf(DummyMove.class);
        assertThat(((CompositeMove) move).getMoves()[1]).isInstanceOf(NoChangeMove.class);

        move = CompositeMove.buildMove(first, second);
        assertThat(move).isInstanceOf(CompositeMove.class);
        assertThat(((CompositeMove) move).getMoves()[0]).isInstanceOf(DummyMove.class);
        assertThat(((CompositeMove) move).getMoves()[1]).isInstanceOf(NoChangeMove.class);
    }

    @Test
    public void isMoveDoable() {
        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        Move first = new DummyMove();
        Move second = mock(DummyMove.class);
        when(second.isMoveDoable(scoreDirector)).thenReturn(false);
        Move move = CompositeMove.buildMove(first, second);
        assertThat(move.isMoveDoable(scoreDirector)).isFalse();
    }

    @Test
    public void equals() {
        Move first = new DummyMove();
        Move second = new NoChangeMove();
        Move move = CompositeMove.buildMove(Arrays.asList(first, second));
        Move other = CompositeMove.buildMove(first, second);
        assertThat(move.equals(other)).isTrue();

        move = CompositeMove.buildMove(first, second);
        other = CompositeMove.buildMove(second, first);
        assertThat(move.equals(other)).isFalse();
        assertThat(move.equals(new DummyMove())).isFalse();
        assertThat(move.equals(move)).isTrue();
    }

    @Test @Ignore("PLANNER-611") // TODO https://issues.jboss.org/browse/PLANNER-611
    public void interconnectedChildMoves() {
        TestdataSolution solution = new TestdataSolution("s1");
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        solution.setValueList(Arrays.asList(v1, v2, v3));
        TestdataEntity e1 = new TestdataEntity("e1", v1);
        TestdataEntity e2 = new TestdataEntity("e2", v2);
        solution.setEntityList(Arrays.asList(e1, e2));

        GenuineVariableDescriptor<TestdataSolution> variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        Move first = new SwapMove<>(Collections.singletonList(variableDescriptor), e1, e2);
        Move second = new ChangeMove(e1, variableDescriptor, v3);
        Move move = CompositeMove.buildMove(first, second);

        assertThat(e1.getValue()).isSameAs(v1);
        assertThat(e2.getValue()).isSameAs(v2);

        ScoreDirector scoreDirector = mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        assertThat(e1.getValue()).isSameAs(v3);
        assertThat(e2.getValue()).isSameAs(v1);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValue()).isSameAs(v1);
        assertThat(e2.getValue()).isSameAs(v2);
    }

}
