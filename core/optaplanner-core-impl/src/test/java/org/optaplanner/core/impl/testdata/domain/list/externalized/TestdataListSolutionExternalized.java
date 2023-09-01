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

package org.optaplanner.core.impl.testdata.domain.list.externalized;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

@PlanningSolution
public class TestdataListSolutionExternalized {

    public static TestdataListSolutionExternalized generateUninitializedSolution(int valueCount, int entityCount) {
        List<TestdataListEntityExternalized> entityList = IntStream.range(0, entityCount)
                .mapToObj(i -> new TestdataListEntityExternalized("Generated Entity " + i))
                .collect(Collectors.toList());
        List<TestdataListValueExternalized> valueList = IntStream.range(0, valueCount)
                .mapToObj(i -> new TestdataListValueExternalized("Generated Value " + i))
                .collect(Collectors.toList());
        TestdataListSolutionExternalized solution = new TestdataListSolutionExternalized();
        solution.setValueList(valueList);
        solution.setEntityList(entityList);
        return solution;
    }

    private List<TestdataListValueExternalized> valueList;
    private List<TestdataListEntityExternalized> entityList;
    private SimpleScore score;

    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    public List<TestdataListValueExternalized> getValueList() {
        return valueList;
    }

    public void setValueList(List<TestdataListValueExternalized> valueList) {
        this.valueList = valueList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataListEntityExternalized> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataListEntityExternalized> entityList) {
        this.entityList = entityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }
}
