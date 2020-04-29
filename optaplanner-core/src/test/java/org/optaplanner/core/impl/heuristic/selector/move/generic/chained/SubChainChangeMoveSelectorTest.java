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

package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.chained.DefaultSubChainSelector;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChainSelector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class SubChainChangeMoveSelectorTest {

    @Test
    public void differentValueDescriptorException() {
        SubChainSelector subChainSelector = mock(DefaultSubChainSelector.class);
        GenuineVariableDescriptor descriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(subChainSelector.getVariableDescriptor()).thenReturn(descriptor);
        EntityIndependentValueSelector valueSelector = mock(EntityIndependentValueSelector.class);
        GenuineVariableDescriptor otherDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(valueSelector.getVariableDescriptor()).thenReturn(otherDescriptor);
        assertThatIllegalStateException().isThrownBy(
                () -> new SubChainChangeMoveSelector(subChainSelector, valueSelector, true, true));
    }

    @Test
    public void determinedSelectionWithNeverEndingChainSelector() {
        SubChainSelector subChainSelector = mock(DefaultSubChainSelector.class);
        when(subChainSelector.isNeverEnding()).thenReturn(true);
        GenuineVariableDescriptor descriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(subChainSelector.getVariableDescriptor()).thenReturn(descriptor);
        EntityIndependentValueSelector valueSelector = mock(EntityIndependentValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(descriptor);
        assertThatIllegalStateException().isThrownBy(
                () -> new SubChainChangeMoveSelector(subChainSelector, valueSelector, false, true));
    }

    @Test
    public void determinedSelectionWithNeverEndingValueSelector() {
        SubChainSelector subChainSelector = mock(DefaultSubChainSelector.class);
        GenuineVariableDescriptor descriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(subChainSelector.getVariableDescriptor()).thenReturn(descriptor);
        EntityIndependentValueSelector valueSelector = mock(EntityIndependentValueSelector.class);
        when(valueSelector.isNeverEnding()).thenReturn(true);
        when(valueSelector.getVariableDescriptor()).thenReturn(descriptor);
        assertThatIllegalStateException().isThrownBy(
                () -> new SubChainChangeMoveSelector(subChainSelector, valueSelector, false, true));
    }

    @Test
    public void isCountable() {
        SubChainSelector subChainSelector = mock(DefaultSubChainSelector.class);
        GenuineVariableDescriptor descriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(subChainSelector.getVariableDescriptor()).thenReturn(descriptor);
        EntityIndependentValueSelector valueSelector = mock(EntityIndependentValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(descriptor);
        SubChainChangeMoveSelector testedSelector =
                new SubChainChangeMoveSelector(subChainSelector, valueSelector, true, true);

        when(subChainSelector.isCountable()).thenReturn(false);
        when(valueSelector.isCountable()).thenReturn(true);
        assertEquals(false, testedSelector.isCountable());

        when(subChainSelector.isCountable()).thenReturn(true);
        when(valueSelector.isCountable()).thenReturn(false);
        assertEquals(false, testedSelector.isCountable());

        when(subChainSelector.isCountable()).thenReturn(true);
        when(valueSelector.isCountable()).thenReturn(true);
        assertEquals(true, testedSelector.isCountable());

        when(subChainSelector.isCountable()).thenReturn(false);
        when(valueSelector.isCountable()).thenReturn(false);
        assertEquals(false, testedSelector.isCountable());
    }

    @Test
    public void getSize() {
        SubChainSelector subChainSelector = mock(DefaultSubChainSelector.class);
        GenuineVariableDescriptor descriptor = TestdataEntity.buildVariableDescriptorForValue();
        when(subChainSelector.getVariableDescriptor()).thenReturn(descriptor);
        EntityIndependentValueSelector valueSelector = mock(EntityIndependentValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(descriptor);
        SubChainChangeMoveSelector testedSelector =
                new SubChainChangeMoveSelector(subChainSelector, valueSelector, true, true);

        when(subChainSelector.getSize()).thenReturn(1L);
        when(valueSelector.getSize()).thenReturn(2L);
        assertEquals(2, testedSelector.getSize());

        when(subChainSelector.getSize()).thenReturn(100L);
        when(valueSelector.getSize()).thenReturn(200L);
        assertEquals(20000, testedSelector.getSize());
    }

}
