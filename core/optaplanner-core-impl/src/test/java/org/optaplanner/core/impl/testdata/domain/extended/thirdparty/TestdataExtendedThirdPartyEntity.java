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

package org.optaplanner.core.impl.testdata.domain.extended.thirdparty;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity
public class TestdataExtendedThirdPartyEntity extends TestdataThirdPartyEntityPojo {

    public static EntityDescriptor<TestdataExtendedThirdPartySolution> buildEntityDescriptor() {
        return TestdataExtendedThirdPartySolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataThirdPartyEntityPojo.class);
    }

    public static GenuineVariableDescriptor<TestdataExtendedThirdPartySolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private Object extraObject;

    public TestdataExtendedThirdPartyEntity() {
    }

    public TestdataExtendedThirdPartyEntity(String code) {
        super(code);
    }

    public TestdataExtendedThirdPartyEntity(String code, TestdataValue value) {
        super(code, value);
    }

    public TestdataExtendedThirdPartyEntity(String code, TestdataValue value, Object extraObject) {
        super(code, value);
        this.extraObject = extraObject;
    }

    public Object getExtraObject() {
        return extraObject;
    }

    public void setExtraObject(Object extraObject) {
        this.extraObject = extraObject;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getValue() {
        return super.getValue();
    }

}
