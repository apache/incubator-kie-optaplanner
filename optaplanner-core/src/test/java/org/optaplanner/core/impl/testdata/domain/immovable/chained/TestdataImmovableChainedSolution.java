/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.testdata.domain.immovable.chained;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;

@PlanningSolution
public class TestdataImmovableChainedSolution extends TestdataObject {

    public static SolutionDescriptor<TestdataImmovableChainedSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataImmovableChainedSolution.class,
                TestdataImmovableChainedEntity.class);
    }

    private List<TestdataChainedAnchor> chainedAnchorList;
    private List<TestdataImmovableChainedEntity> chainedEntityList;

    private SimpleScore score;

    public TestdataImmovableChainedSolution() {
    }

    public TestdataImmovableChainedSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "chainedAnchorRange")
    @ProblemFactCollectionProperty
    public List<TestdataChainedAnchor> getChainedAnchorList() {
        return chainedAnchorList;
    }

    public void setChainedAnchorList(List<TestdataChainedAnchor> chainedAnchorList) {
        this.chainedAnchorList = chainedAnchorList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "chainedEntityRange")
    public List<TestdataImmovableChainedEntity> getChainedEntityList() {
        return chainedEntityList;
    }

    public void setChainedEntityList(List<TestdataImmovableChainedEntity> chainedEntityList) {
        this.chainedEntityList = chainedEntityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
