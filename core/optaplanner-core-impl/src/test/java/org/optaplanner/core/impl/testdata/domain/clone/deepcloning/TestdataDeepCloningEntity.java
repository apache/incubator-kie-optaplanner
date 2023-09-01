/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.testdata.domain.clone.deepcloning;

import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.DummyVariableListener;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity
public class TestdataDeepCloningEntity extends TestdataObject {

    public static EntityDescriptor<TestdataDeepCloningSolution> buildEntityDescriptor() {
        return TestdataDeepCloningSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataDeepCloningEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataDeepCloningSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataValue value;
    private TestdataVariousTypes unannotatedCopiedTestdataVariousTypes;
    private TestdataVariousTypes unannotatedClonedTestdataVariousTypes;
    private TestdataVariousTypes annotatedClonedTestdataVariousTypes;
    private TestdataVariousTypes sameValueAsUnannotatedClonedTestdataVariousTypes;
    private AnnotatedTestdataVariousTypes annotatedTestdataVariousTypes;
    private List<String> shadowVariableList;
    private Map<String, String> shadowVariableMap;

    public TestdataDeepCloningEntity() {
    }

    public TestdataDeepCloningEntity(String code) {
        super(code);
    }

    public TestdataDeepCloningEntity(String code, TestdataValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getValue() {
        return value;
    }

    public void setValue(TestdataValue value) {
        this.value = value;
    }

    /**
     * Neither the type nor the method or the field are annotated; therefore we expect a shallow clone.
     *
     * @return
     */
    public TestdataVariousTypes getUnannotatedCopiedTestdataVariousTypes() {
        return unannotatedCopiedTestdataVariousTypes;
    }

    public void setUnannotatedCopiedTestdataVariousTypes(TestdataVariousTypes unannotatedCopiedTestdataVariousTypes) {
        this.unannotatedCopiedTestdataVariousTypes = unannotatedCopiedTestdataVariousTypes;
    }

    /**
     * Even though the return type is not annotated, the method is annotated, therefore we expect a deep clone.
     *
     * @return
     */
    @DeepPlanningClone
    public TestdataVariousTypes getUnannotatedClonedTestdataVariousTypes() {
        return unannotatedClonedTestdataVariousTypes;
    }

    public void setUnannotatedClonedTestdataVariousTypes(TestdataVariousTypes unannotatedClonedTestdataVariousTypes) {
        this.unannotatedClonedTestdataVariousTypes = unannotatedClonedTestdataVariousTypes;
    }

    /**
     * This field originally gets the same value as {@link #getUnannotatedClonedTestdataVariousTypes()},
     * but unlike that field, it is not annotated to be deep-cloned.
     * Therefore we expect a shallow clone, even though the two methods on a will then return different instances.
     *
     * @return
     */
    public TestdataVariousTypes getSameValueAsUnannotatedClonedTestdataVariousTypes() {
        return sameValueAsUnannotatedClonedTestdataVariousTypes;
    }

    public void setSameValueAsUnannotatedClonedTestdataVariousTypes(
            TestdataVariousTypes sameValueAsUnannotatedClonedTestdataVariousTypes) {
        this.sameValueAsUnannotatedClonedTestdataVariousTypes = sameValueAsUnannotatedClonedTestdataVariousTypes;
    }

    /**
     * Neither the return type nor the method are annotated, but the instance returned is of an annotated type.
     * Therefore we expect a deep clone.
     *
     * @return
     */
    public TestdataVariousTypes getAnnotatedClonedTestdataVariousTypes() {
        return annotatedClonedTestdataVariousTypes;
    }

    public void setAnnotatedClonedTestdataVariousTypes(TestdataVariousTypes annotatedClonedTestdataVariousTypes) {
        if (annotatedClonedTestdataVariousTypes != null
                && annotatedClonedTestdataVariousTypes.getClass() != AnnotatedTestdataVariousTypes.class) {
            throw new IllegalArgumentException("Unexpected value: " + annotatedClonedTestdataVariousTypes);
        }
        this.annotatedClonedTestdataVariousTypes = annotatedClonedTestdataVariousTypes;
    }

    /**
     * The return type is annotated. Therefore we expect a deep clone.
     *
     * @return
     */
    public AnnotatedTestdataVariousTypes getAnnotatedTestdataVariousTypes() {
        return annotatedTestdataVariousTypes;
    }

    public void setAnnotatedTestdataVariousTypes(AnnotatedTestdataVariousTypes annotatedTestdataVariousTypes) {
        this.annotatedTestdataVariousTypes = annotatedTestdataVariousTypes;
    }

    @DeepPlanningClone
    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "value")
    public List<String> getShadowVariableList() {
        return shadowVariableList;
    }

    public void setShadowVariableList(List<String> shadowVariableList) {
        this.shadowVariableList = shadowVariableList;
    }

    @DeepPlanningClone
    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "value")
    public Map<String, String> getShadowVariableMap() {
        return shadowVariableMap;
    }

    public void setShadowVariableMap(Map<String, String> shadowVariableMap) {
        this.shadowVariableMap = shadowVariableMap;
    }

}
