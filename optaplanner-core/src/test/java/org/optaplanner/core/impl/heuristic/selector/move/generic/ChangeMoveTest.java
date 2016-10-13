/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.entityproviding.TestdataEntityProvidingEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataOtherValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCode;

public class ChangeMoveTest {

    @Test
    public void isMoveDoable() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        ChangeMove aMove = new ChangeMove(a, entityDescriptor.getGenuineVariableDescriptor("value"), v2);
        a.setValue(v1);
        assertThat(aMove.isMoveDoable(scoreDirector)).isEqualTo(true);

        a.setValue(v2);
        assertThat(aMove.isMoveDoable(scoreDirector)).isEqualTo(false);

        a.setValue(v3);
        assertThat(aMove.isMoveDoable(scoreDirector)).isEqualTo(true);
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        ChangeMove aMove = new ChangeMove(a, entityDescriptor.getGenuineVariableDescriptor("value"), v2);
        a.setValue(v1);
        aMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);

        a.setValue(v2);
        aMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);

        a.setValue(v3);
        aMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
    }

    @Test
    public void getters() {
        ChangeMove move = new ChangeMove(new TestdataMultiVarEntity("a"),
                TestdataMultiVarEntity.buildVariableDescriptorForPrimaryValue(), null);
        assertCode("a", move.getEntity());
        assertThat(move.getVariableName()).isEqualTo("primaryValue");
        assertCode(null, move.getToPlanningValue());

        move = new ChangeMove(new TestdataMultiVarEntity("b"),
                TestdataMultiVarEntity.buildVariableDescriptorForSecondaryValue(), new TestdataValue("1"));
        assertCode("b", move.getEntity());
        assertThat(move.getVariableName()).isEqualTo("secondaryValue");
        assertCode("1", move.getToPlanningValue());
    }

    @Test
    public void toStringTest() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataEntity a = new TestdataEntity("a", null);
        TestdataEntity b = new TestdataEntity("b", v1);
        GenuineVariableDescriptor variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();

        assertEquals("a {null -> null}", new ChangeMove(a, variableDescriptor, null).toString());
        assertEquals("a {null -> v1}", new ChangeMove(a, variableDescriptor, v1).toString());
        assertEquals("a {null -> v2}", new ChangeMove(a, variableDescriptor, v2).toString());
        assertEquals("b {v1 -> null}", new ChangeMove(b, variableDescriptor, null).toString());
        assertEquals("b {v1 -> v1}", new ChangeMove(b, variableDescriptor, v1).toString());
        assertEquals("b {v1 -> v2}", new ChangeMove(b, variableDescriptor, v2).toString());
    }

    @Test
    public void toStringTestMultiVar() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        TestdataValue v4 = new TestdataValue("v4");
        TestdataOtherValue w1 = new TestdataOtherValue("w1");
        TestdataOtherValue w2 = new TestdataOtherValue("w2");
        TestdataMultiVarEntity a = new TestdataMultiVarEntity("a", null, null, null);
        TestdataMultiVarEntity b = new TestdataMultiVarEntity("b", v1, v3, w1);
        TestdataMultiVarEntity c = new TestdataMultiVarEntity("c", v2, v4, w2);
        EntityDescriptor entityDescriptor = TestdataMultiVarEntity.buildEntityDescriptor();
        GenuineVariableDescriptor variableDescriptor = entityDescriptor.getGenuineVariableDescriptor("secondaryValue");

        assertEquals("a {null -> null}", new ChangeMove(a, variableDescriptor, null).toString());
        assertEquals("a {null -> v1}", new ChangeMove(a, variableDescriptor, v1).toString());
        assertEquals("a {null -> v2}", new ChangeMove(a, variableDescriptor, v2).toString());
        assertEquals("b {v3 -> null}", new ChangeMove(b, variableDescriptor, null).toString());
        assertEquals("b {v3 -> v1}", new ChangeMove(b, variableDescriptor, v1).toString());
        assertEquals("b {v3 -> v2}", new ChangeMove(b, variableDescriptor, v2).toString());
        assertEquals("c {v4 -> v3}", new ChangeMove(c, variableDescriptor, v3).toString());
    }

}
