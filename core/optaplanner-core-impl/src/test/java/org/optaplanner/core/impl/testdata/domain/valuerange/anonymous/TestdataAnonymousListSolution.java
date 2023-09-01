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

package org.optaplanner.core.impl.testdata.domain.valuerange.anonymous;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningSolution
public class TestdataAnonymousListSolution extends TestdataObject {

    public static SolutionDescriptor<TestdataAnonymousListSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataAnonymousListSolution.class,
                TestdataAnonymousValueRangeEntity.class);
    }

    private List<TestdataAnonymousValueRangeEntity> entityList;

    private SimpleScore score;

    public TestdataAnonymousListSolution() {
    }

    public TestdataAnonymousListSolution(String code) {
        super(code);
    }

    @PlanningEntityCollectionProperty
    public List<TestdataAnonymousValueRangeEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataAnonymousValueRangeEntity> entityList) {
        this.entityList = entityList;
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

    @ValueRangeProvider
    public List<Integer> createIntegerList() {
        return List.of(0, 1);
    }

    @ValueRangeProvider
    public List<Long> createLongList() {
        return List.of(0L, 1L);
    }

    @ValueRangeProvider
    public List<Number> createNumberList() {
        return List.of(0, BigInteger.TEN);
    }

    @ValueRangeProvider
    public List<BigInteger> createBigIntegerList() {
        return List.of(BigInteger.ZERO, BigInteger.TEN);
    }

    @ValueRangeProvider
    public List<BigDecimal> createBigDecimalList() {
        return List.of(BigDecimal.ZERO, BigDecimal.TEN);
    }

}
