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

package org.optaplanner.core.impl.testdata.domain.multivar;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity
public class TestdataMultiVarEntity extends TestdataObject {

    public static EntityDescriptor<TestdataMultiVarSolution> buildEntityDescriptor() {
        return TestdataMultiVarSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataMultiVarEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataMultiVarSolution> buildVariableDescriptorForPrimaryValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("primaryValue");
    }

    public static GenuineVariableDescriptor<TestdataMultiVarSolution> buildVariableDescriptorForSecondaryValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("secondaryValue");
    }

    public static GenuineVariableDescriptor<TestdataMultiVarSolution> buildVariableDescriptorForTertiaryNullableValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("tertiaryNullableValue");
    }

    private TestdataValue primaryValue;
    private TestdataValue secondaryValue;

    private TestdataOtherValue tertiaryNullableValue;

    public TestdataMultiVarEntity() {
    }

    public TestdataMultiVarEntity(String code) {
        super(code);
    }

    public TestdataMultiVarEntity(String code, TestdataValue primaryValue, TestdataValue secondaryValue,
            TestdataOtherValue tertiaryNullableValue) {
        super(code);
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
        this.tertiaryNullableValue = tertiaryNullableValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(TestdataValue primaryValue) {
        this.primaryValue = primaryValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(TestdataValue secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "otherValueRange")
    public TestdataOtherValue getTertiaryNullableValue() {
        return tertiaryNullableValue;
    }

    public void setTertiaryNullableValue(TestdataOtherValue tertiaryNullableValue) {
        this.tertiaryNullableValue = tertiaryNullableValue;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
