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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Arrays;

import org.junit.Test;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.entityproviding.TestdataEntityProvidingEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class PillarChangeMoveTest {

    @Test
    public void isMoveDoableValueRangeProviderOnEntity() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        GenuineVariableDescriptor variableDescriptor = TestdataEntityProvidingEntity.buildVariableDescriptorForValue();

        PillarChangeMove abMove;
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v1);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v2);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v3);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v4);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        GenuineVariableDescriptor variableDescriptor = TestdataEntityProvidingEntity.buildVariableDescriptorForValue();

        PillarChangeMove abMove = new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v2);

        a.setValue(v3);
        b.setValue(v3);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v2);

        a.setValue(v2);
        b.setValue(v2);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v2);

        PillarChangeMove abcMove = new PillarChangeMove(Arrays.<Object>asList(a, b, c), variableDescriptor, v2);

        a.setValue(v2);
        b.setValue(v2);
        c.setValue(v2);
        abcMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v2);
        assertThat(c.getValue()).isEqualTo(v2);

        a.setValue(v3);
        b.setValue(v3);
        c.setValue(v3);
        abcMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v2);
        assertThat(c.getValue()).isEqualTo(v2);
    }

    @Test
    public void getters() {
        PillarChangeMove move = new PillarChangeMove(
                Arrays.<Object>asList(new TestdataMultiVarEntity("a"), new TestdataMultiVarEntity("b")),
                TestdataMultiVarEntity.buildVariableDescriptorForPrimaryValue(), null);
        assertAllCodesOfCollection(move.getPillar(), "a", "b");
        assertThat(move.getVariableName()).isEqualTo("primaryValue");
        assertThat(move.getToPlanningValue()).isNull();

        move = new PillarChangeMove(
                Arrays.<Object>asList(new TestdataMultiVarEntity("c"), new TestdataMultiVarEntity("d")),
                TestdataMultiVarEntity.buildVariableDescriptorForSecondaryValue(), new TestdataValue("1"));
        assertAllCodesOfCollection(move.getPillar(), "c", "d");
        assertThat(move.getVariableName()).isEqualTo("secondaryValue");
        assertCode("1", move.getToPlanningValue());
    }

    @Test
    public void toStringTest() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataEntity a = new TestdataEntity("a", null);
        TestdataEntity b = new TestdataEntity("b", null);
        TestdataEntity c = new TestdataEntity("c", v1);
        TestdataEntity d = new TestdataEntity("d", v1);
        TestdataEntity e = new TestdataEntity("e", v1);
        GenuineVariableDescriptor variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();

        assertThat(new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v1).toString()).isEqualTo("[a, b] {null -> v1}");
        assertThat(new PillarChangeMove(Arrays.<Object>asList(a, b), variableDescriptor, v2).toString()).isEqualTo("[a, b] {null -> v2}");
        assertThat(new PillarChangeMove(Arrays.<Object>asList(c, d, e), variableDescriptor, null).toString()).isEqualTo("[c, d, e] {v1 -> null}");
        assertThat(new PillarChangeMove(Arrays.<Object>asList(c, d, e), variableDescriptor, v2).toString()).isEqualTo("[c, d, e] {v1 -> v2}");
        assertThat(new PillarChangeMove(Arrays.<Object>asList(d), variableDescriptor, v2).toString()).isEqualTo("[d] {v1 -> v2}");
    }

}
