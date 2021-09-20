/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.minizinc.backend;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.domain.valuerange.buildin.primboolean.BooleanValueRange;

@PlanningSolution
public class MyPlanningSolution {
    @PlanningEntityCollectionProperty
    List<MyIntVariable> intVariableList;

    @PlanningEntityCollectionProperty
    List<MySecondIntVariable> secondIntVariableList;

    @PlanningEntityCollectionProperty
    List<ConstrainedIntVariable> constrainedIntVariableList;

    @PlanningEntityCollectionProperty
    List<MyIntArrayVariable> intArrayVariableList;

    @PlanningEntityCollectionProperty
    List<MyBoolVariable> boolVariableList;

    @PlanningEntityCollectionProperty
    List<MySecondBoolVariable> secondBoolVariableList;

    @PlanningEntityCollectionProperty
    List<ConstrainedBoolVariable> constrainedBoolVariableList;

    @PlanningEntityCollectionProperty
    List<MyBoolArrayVariable> boolArrayVariableList;

    @PlanningEntityCollectionProperty
    List<MySecondBoolArrayVariable> secondBoolArrayVariableList;

    @ValueRangeProvider(id = "valueRange")
    List<Integer> valueRange;

    @ValueRangeProvider(id = "booleanRange")
    BooleanValueRange booleanValueRange;

    @PlanningScore
    HardSoftScore hardSoftScore;
}
