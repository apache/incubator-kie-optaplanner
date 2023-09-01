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

package org.optaplanner.core.impl.testdata.domain.multientity;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataHerdEntity extends TestdataObject {

    public static EntityDescriptor<TestdataMultiEntitySolution> buildEntityDescriptor() {
        return TestdataMultiEntitySolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataHerdEntity.class);
    }

    private TestdataLeadEntity leadEntity;

    public TestdataHerdEntity() {
    }

    public TestdataHerdEntity(String code) {
        super(code);
    }

    public TestdataHerdEntity(String code, TestdataLeadEntity leadEntity) {
        super(code);
        this.leadEntity = leadEntity;
    }

    @PlanningVariable(valueRangeProviderRefs = "leadEntityRange")
    public TestdataLeadEntity getLeadEntity() {
        return leadEntity;
    }

    public void setLeadEntity(TestdataLeadEntity leadEntity) {
        this.leadEntity = leadEntity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
