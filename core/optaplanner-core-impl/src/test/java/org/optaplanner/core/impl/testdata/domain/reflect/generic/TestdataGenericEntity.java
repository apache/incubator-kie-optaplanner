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

package org.optaplanner.core.impl.testdata.domain.reflect.generic;

import java.util.Map;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataGenericEntity<T> extends TestdataObject {

    public static EntityDescriptor<TestdataGenericSolution> buildEntityDescriptor() {
        return TestdataGenericSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataGenericEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataGenericSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataGenericValue<T> value;
    private TestdataGenericValue<T> subTypeValue;
    private TestdataGenericValue<Map<T, TestdataGenericValue<T>>> complexGenericValue;

    public TestdataGenericEntity() {
    }

    public TestdataGenericEntity(String code) {
        super(code);
    }

    public TestdataGenericEntity(String code, TestdataGenericValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataGenericValue<T> getValue() {
        return value;
    }

    @PlanningVariable(valueRangeProviderRefs = "subTypeValueRange")
    public TestdataGenericValue<T> getSubTypeValue() {
        return subTypeValue;
    }

    public void setValue(TestdataGenericValue<T> value) {
        this.value = value;
    }

    public void setSubTypeValue(TestdataGenericValue<T> subTypeValue) {
        this.subTypeValue = subTypeValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "complexGenericValueRange")
    public TestdataGenericValue<Map<T, TestdataGenericValue<T>>> getComplexGenericValue() {
        return complexGenericValue;
    }

    public void setComplexGenericValue(TestdataGenericValue<Map<T, TestdataGenericValue<T>>> complexGenericValue) {
        this.complexGenericValue = complexGenericValue;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
