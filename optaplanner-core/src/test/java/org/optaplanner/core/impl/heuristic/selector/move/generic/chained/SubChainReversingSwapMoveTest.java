/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import java.util.Arrays;

import org.optaplanner.core.impl.domain.entity.PlanningEntityDescriptor;
import org.optaplanner.core.impl.domain.variable.PlanningVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChain;
import org.optaplanner.core.impl.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SubChainReversingSwapMoveTest {

    @Test
    public void noTrailing() {
        PlanningEntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        PlanningVariableDescriptor variableDescriptor = entityDescriptor.getVariableDescriptor("chainedObject");
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        TestdataChainedEntity a5 = new TestdataChainedEntity("a5", a4);

        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        TestdataChainedEntity b2 = new TestdataChainedEntity("b2", b1);
        TestdataChainedEntity b3 = new TestdataChainedEntity("b3", b2);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, a5, b1, b2, b3});

        SubChainReversingSwapMove move = new SubChainReversingSwapMove(variableDescriptor,
                new SubChain(Arrays.<Object>asList(a3, a4, a5)),
                new SubChain(Arrays.<Object>asList(b2, b3)));
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        SelectorTestUtils.assertChain(a0, a1, a2, b3, b2);
        SelectorTestUtils.assertChain(b0, b1, a5, a4, a3);

        verify(scoreDirector).beforeVariableChanged(a3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a3, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a4, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a4, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a5, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(b2, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(b2, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(b3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(b3, "chainedObject");

        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4, a5);
        SelectorTestUtils.assertChain(b0, b1, b2, b3);
    }

    @Test
    public void noTrailingInPlace() {
        PlanningEntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        PlanningVariableDescriptor variableDescriptor = entityDescriptor.getVariableDescriptor("chainedObject");
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        TestdataChainedEntity a5 = new TestdataChainedEntity("a5", a4);
        TestdataChainedEntity a6 = new TestdataChainedEntity("a6", a5);
        TestdataChainedEntity a7 = new TestdataChainedEntity("a7", a6);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, a5, a6, a7});

        SubChainReversingSwapMove move = new SubChainReversingSwapMove(variableDescriptor,
                new SubChain(Arrays.<Object>asList(a3, a4, a5)),
                new SubChain(Arrays.<Object>asList(a6, a7)));
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        SelectorTestUtils.assertChain(a0, a1, a2, a7, a6, a5, a4, a3);

        verify(scoreDirector).beforeVariableChanged(a3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a3, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a4, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a4, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a6, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a6, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a7, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a7, "chainedObject");

        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4, a5, a6, a7);
    }

    @Test
    public void oldAndNewTrailing() {
        PlanningEntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        PlanningVariableDescriptor variableDescriptor = entityDescriptor.getVariableDescriptor("chainedObject");
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        TestdataChainedEntity a5 = new TestdataChainedEntity("a5", a4);

        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        TestdataChainedEntity b2 = new TestdataChainedEntity("b2", b1);
        TestdataChainedEntity b3 = new TestdataChainedEntity("b3", b2);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, a5, b1, b2, b3});

        SubChainReversingSwapMove move = new SubChainReversingSwapMove(variableDescriptor,
                new SubChain(Arrays.<Object>asList(a2, a3, a4)),
                new SubChain(Arrays.<Object>asList(b1, b2)));
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        SelectorTestUtils.assertChain(a0, a1, b2, b1, a5);
        SelectorTestUtils.assertChain(b0, a4, a3, a2, b3);

        verify(scoreDirector).beforeVariableChanged(a2, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a2, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a3, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a4, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a4, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(b1, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(b1, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(b2, "chainedObject");
        verify(scoreDirector).afterVariableChanged(b2, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(b3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(b3, "chainedObject");

        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4, a5);
        SelectorTestUtils.assertChain(b0, b1, b2, b3);
    }

    @Test
    public void oldAndNewTrailingInPlace() {
        PlanningEntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        PlanningVariableDescriptor variableDescriptor = entityDescriptor.getVariableDescriptor("chainedObject");
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        TestdataChainedEntity a5 = new TestdataChainedEntity("a5", a4);
        TestdataChainedEntity a6 = new TestdataChainedEntity("a6", a5);
        TestdataChainedEntity a7 = new TestdataChainedEntity("a7", a6);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, a5, a6, a7});

        SubChainReversingSwapMove move = new SubChainReversingSwapMove(variableDescriptor,
                new SubChain(Arrays.<Object>asList(a2, a3, a4)),
                new SubChain(Arrays.<Object>asList(a5, a6)));
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        SelectorTestUtils.assertChain(a0, a1, a6, a5, a4, a3, a2, a7);

        verify(scoreDirector).beforeVariableChanged(a2, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a2, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a3, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a4, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a4, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a5, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a5, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a6, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a6, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a7, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a7, "chainedObject");

        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4, a5, a6, a7);
    }

    @Test
    public void oldAndNewTrailingInPlaceOppositeParameterOrder() {
        PlanningEntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        PlanningVariableDescriptor variableDescriptor = entityDescriptor.getVariableDescriptor("chainedObject");
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        TestdataChainedEntity a5 = new TestdataChainedEntity("a5", a4);
        TestdataChainedEntity a6 = new TestdataChainedEntity("a6", a5);
        TestdataChainedEntity a7 = new TestdataChainedEntity("a7", a6);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, a5, a6, a7});

        SubChainReversingSwapMove move = new SubChainReversingSwapMove(variableDescriptor,
                new SubChain(Arrays.<Object>asList(a5, a6)), // Opposite parameter order
                new SubChain(Arrays.<Object>asList(a2, a3, a4)));
        Move undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);

        SelectorTestUtils.assertChain(a0, a1, a6, a5, a4, a3, a2, a7);

        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a2, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a2, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a3, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a3, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a4, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a4, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a5, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a5, "chainedObject");
        verify(scoreDirector).beforeVariableChanged(a6, "chainedObject");
        verify(scoreDirector).afterVariableChanged(a6, "chainedObject");
        verify(scoreDirector, atLeastOnce()).beforeVariableChanged(a7, "chainedObject");
        verify(scoreDirector, atLeastOnce()).afterVariableChanged(a7, "chainedObject");

        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4, a5, a6, a7);
    }

}
