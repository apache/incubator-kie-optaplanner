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

package org.optaplanner.core.impl.testdata.domain.solutionproperties.autodiscover;

import java.util.List;

import org.optaplanner.core.api.domain.autodiscover.AutoDiscoverMemberType;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningSolution(autoDiscoverMemberType = AutoDiscoverMemberType.GETTER)
public class TestdataExtendedAutoDiscoverGetterSolution extends TestdataAutoDiscoverGetterSolution {

    public static SolutionDescriptor<TestdataExtendedAutoDiscoverGetterSolution> buildSubclassSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataExtendedAutoDiscoverGetterSolution.class,
                TestdataEntity.class);
    }

    private TestdataObject singleProblemFactFieldOverride;
    private List<TestdataValue> problemFactListFieldOverride;

    private List<TestdataEntity> entityListFieldOverride;
    private TestdataEntity otherEntityFieldOverride;

    public TestdataExtendedAutoDiscoverGetterSolution() {
    }

    public TestdataExtendedAutoDiscoverGetterSolution(String code) {
        super(code);
    }

    public TestdataExtendedAutoDiscoverGetterSolution(String code, TestdataObject singleProblemFact,
            List<TestdataValue> problemFactList, List<TestdataEntity> entityList,
            TestdataEntity otherEntity) {
        super(code);
        this.singleProblemFactFieldOverride = singleProblemFact;
        this.problemFactListFieldOverride = problemFactList;
        this.entityListFieldOverride = entityList;
        this.otherEntityFieldOverride = otherEntity;
    }

    @Override
    public TestdataObject getSingleProblemFact() {
        return singleProblemFactFieldOverride;
    }

    @ProblemFactProperty // Override from a fact collection to a single fact
    @ValueRangeProvider(id = "valueRange")
    @Override
    public List<TestdataValue> getProblemFactList() {
        return problemFactListFieldOverride;
    }

    @Override
    public List<TestdataEntity> getEntityList() {
        return entityListFieldOverride;
    }

    @Override
    public TestdataEntity getOtherEntity() {
        return otherEntityFieldOverride;
    }

}
