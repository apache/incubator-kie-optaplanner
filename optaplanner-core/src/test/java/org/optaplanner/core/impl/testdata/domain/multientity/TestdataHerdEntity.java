/*
 * Copyright 2013 JBoss Inc
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

package org.optaplanner.core.impl.testdata.domain.multientity;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.value.ValueRange;
import org.optaplanner.core.api.domain.value.ValueRangeType;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.PlanningEntityDescriptor;
import org.optaplanner.core.impl.domain.solution.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataHerdEntity extends TestdataObject {

    public static PlanningEntityDescriptor buildEntityDescriptor() {
        SolutionDescriptor solutionDescriptor = TestdataMultiEntitySolution.buildSolutionDescriptor();
        return solutionDescriptor.getEntityDescriptor(TestdataHerdEntity.class);
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

    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "leadEntityList")
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
