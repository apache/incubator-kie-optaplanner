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

package org.optaplanner.core.impl.testdata.domain.shadow.inverserelation;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataInverseRelationEntity extends TestdataObject {

    public static EntityDescriptor<TestdataInverseRelationSolution> buildEntityDescriptor() {
        return TestdataInverseRelationSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataInverseRelationEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataInverseRelationSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataInverseRelationValue value;

    public TestdataInverseRelationEntity() {
    }

    public TestdataInverseRelationEntity(String code) {
        super(code);
    }

    public TestdataInverseRelationEntity(String code, TestdataInverseRelationValue value) {
        this(code);
        this.value = value;
        if (value != null) {
            value.getEntities().add(this);
        }
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange", nullable = true)
    public TestdataInverseRelationValue getValue() {
        return value;
    }

    public void setValue(TestdataInverseRelationValue value) {
        this.value = value;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
