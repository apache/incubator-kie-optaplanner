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
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.extended.TestdataUnannotatedExtendedEntity;

@PlanningSolution(autoDiscoverMemberType = AutoDiscoverMemberType.GETTER)
public class TestdataAutoDiscoverUnannotatedEntitySolution extends TestdataObject {

    public static SolutionDescriptor<TestdataAutoDiscoverUnannotatedEntitySolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(
                TestdataAutoDiscoverUnannotatedEntitySolution.class, TestdataEntity.class);
    }

    private TestdataObject singleProblemFactField;
    private List<TestdataValue> problemFactListField;

    private List<TestdataUnannotatedExtendedEntity> entityListField;
    private TestdataUnannotatedExtendedEntity otherEntityField;

    private SimpleScore score;

    public TestdataAutoDiscoverUnannotatedEntitySolution() {
    }

    public TestdataAutoDiscoverUnannotatedEntitySolution(String code) {
        super(code);
    }

    public TestdataAutoDiscoverUnannotatedEntitySolution(String code, TestdataObject singleProblemFact,
            List<TestdataValue> problemFactList, List<TestdataUnannotatedExtendedEntity> entityList,
            TestdataUnannotatedExtendedEntity otherEntity) {
        super(code);
        this.singleProblemFactField = singleProblemFact;
        this.problemFactListField = problemFactList;
        this.entityListField = entityList;
        this.otherEntityField = otherEntity;
    }

    public TestdataObject getSingleProblemFact() {
        return singleProblemFactField;
    }

    @ValueRangeProvider(id = "valueRange")
    public List<TestdataValue> getProblemFactList() {
        return problemFactListField;
    }

    // should be auto discovered as an entity collection
    public List<TestdataUnannotatedExtendedEntity> getEntityList() {
        return entityListField;
    }

    // should be auto discovered as a single entity property
    public TestdataUnannotatedExtendedEntity getOtherEntity() {
        return otherEntityField;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

}
