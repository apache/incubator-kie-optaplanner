package org.optaplanner.core.config.anno.entity;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.ValueRangeDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntityParent;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubBrokenMissingEntityAnnotation;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubBrokenVarOverride;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubBrokenVarRemoved;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubChangeValRange;
import org.optaplanner.core.impl.testdata.domain.anno.entity.TestdataAnnoEntitySubWith2ndVariable;

import static org.junit.Assert.*;

public class TestdataAnnoTest {

    @Test
    public void parentEntityDescriptor() {
        EntityDescriptor<TestdataAnnoEntityParent.Solution> ed = TestdataAnnoEntityParent.buildParentEntityDescriptor();
        assertEquals(1, ed.getDeclaredVariableDescriptors().size());
        TestdataAnnoEntityParent.buildParentVariableDescriptorForVar1();
    }

    // sub entity should have 2 variables. currently fails
    @Test
    public void subEntityDescriptorWith2ndVariableField() {
        EntityDescriptor<TestdataAnnoEntitySubWith2ndVariable.Solution> ed
                = TestdataAnnoEntitySubWith2ndVariable.buildEntityDescriptor();
        assertEquals(1, ed.getDeclaredVariableDescriptors().size());
        assertEquals(2, ed.getVariableDescriptors().size());
        assertEquals(2, ed.getGenuineVariableCount());
        Assert.assertTrue(
                ed.getGenuineVariableDescriptors().stream()
                .map(GenuineVariableDescriptor::getVariableName)
                .collect(Collectors.toList())
                .containsAll(Arrays.asList("var1", "var2")));

        assertNotNull(TestdataAnnoEntitySubWith2ndVariable.buildVariableDescriptorForVar2());
        assertNotNull(TestdataAnnoEntitySubWith2ndVariable.buildVariableDescriptorForVar1());
    }

    @Test
    public void changeValueRange() {
        EntityDescriptor<TestdataAnnoEntityParent.Solution> parentEd
                = TestdataAnnoEntityParent.buildParentEntityDescriptor();
        assertEquals(1, parentEd.getVariableDescriptors().size());
        assertEquals(1, parentEd.getDeclaredVariableDescriptors().size());

        EntityDescriptor<TestdataAnnoEntitySubChangeValRange.Solution> ed
                = TestdataAnnoEntitySubChangeValRange.buildEntityDescriptor();
        assertEquals(1, ed.getVariableDescriptors().size());
        assertEquals(1, ed.getDeclaredVariableDescriptors().size());
        assertNotNull(TestdataAnnoEntitySubChangeValRange.buildVariableDescriptorForVar1());
        ValueRangeDescriptor<TestdataAnnoEntitySubChangeValRange.Solution> vrd
                = TestdataAnnoEntitySubChangeValRange.buildVariableDescriptorForVar1().getValueRangeDescriptor();
        ValueRange<String> vr = (ValueRange<String>) vrd.extractValueRange(
                new TestdataAnnoEntitySubChangeValRange.Solution() {},
                new TestdataAnnoEntitySubChangeValRange());
        assertTrue(vr.contains("B"));
        assertFalse(vr.contains("A"));

    }

    @Test(expected = IllegalStateException.class)
    public void planningVariableOnFieldAndGetter() {
        assertNull(TestdataAnnoEntitySubBrokenVarFieldAndGetterOverride.buildEntityDescriptor());
    }

    // overriding pl.var in a class that isn't pl.ent is unsupported (can't create Ent.Descr)
    @Test(expected = IllegalStateException.class)
    public void overridingPlanningVarFieldRequiresPlanningEntity() {
        assertNull(TestdataAnnoEntitySubBrokenMissingEntityAnnotation.buildEntityDescriptor());
    }

    // changing anno type unsupported, fail fast currently not implemented
    @Test(expected = IllegalArgumentException.class)
    public void cannotOverrideToDifferentAnnotation() {
        assertNull(TestdataAnnoEntitySubBrokenVarOverride.buildEntityDescriptor());
    }

    // should(?) fail fast, currently not implemented
    @Test(expected = IllegalArgumentException.class)
    public void cannotRemoveAnnotation() {
        EntityDescriptor<TestdataAnnoEntitySubBrokenVarRemoved.Solution> ed
                = TestdataAnnoEntitySubBrokenVarRemoved.buildEntityDescriptor();
        assertEquals(1, ed.getDeclaredVariableDescriptors().size());
        assertEquals("Parent entity's variable should be inherited", 2, ed.getVariableDescriptors().size());
    }
}
