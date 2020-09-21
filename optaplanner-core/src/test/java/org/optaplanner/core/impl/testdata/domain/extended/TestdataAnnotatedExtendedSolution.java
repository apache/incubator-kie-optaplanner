/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.testdata.domain.extended;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningSolution
public class TestdataAnnotatedExtendedSolution extends TestdataSolution {

    public static SolutionDescriptor<TestdataAnnotatedExtendedSolution> buildExtendedSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataAnnotatedExtendedSolution.class,
                TestdataEntity.class, TestdataAnnotatedExtendedEntity.class);
    }

    public static TestdataAnnotatedExtendedSolution generateSolution() {
        return generateSolution(5, 7);
    }

    public static TestdataAnnotatedExtendedSolution generateSolution(int valueListSize, int entityListSize) {
        TestdataAnnotatedExtendedSolution solution = new TestdataAnnotatedExtendedSolution("Generated Solution 0");
        List<TestdataValue> valueList = new ArrayList<>(valueListSize);
        for (int i = 0; i < valueListSize; i++) {
            TestdataValue value = new TestdataValue("Generated Value " + i);
            valueList.add(value);
        }
        solution.setValueList(valueList);
        List<TestdataEntity> entityList = new ArrayList<>(entityListSize);
        for (int i = 0; i < entityListSize; i++) {
            TestdataValue value = valueList.get(i % valueListSize);
            TestdataEntity entity = new TestdataAnnotatedExtendedEntity("Generated Entity " + i, value);
            entityList.add(entity);
        }
        solution.setEntityList(entityList);
        return solution;
    }

    private List<TestdataValue> subValueList;

    private List<TestdataAnnotatedExtendedEntity> subEntityList;

    public TestdataAnnotatedExtendedSolution() {
    }

    public TestdataAnnotatedExtendedSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "subValueRange")
    @ProblemFactCollectionProperty
    public List<TestdataValue> getSubValueList() {
        return subValueList;
    }

    public void setSubValueList(List<TestdataValue> subValueList) {
        this.subValueList = subValueList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataAnnotatedExtendedEntity> getSubEntityList() {
        return subEntityList;
    }

    public void setSubEntityList(List<TestdataAnnotatedExtendedEntity> subEntityList) {
        this.subEntityList = subEntityList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
