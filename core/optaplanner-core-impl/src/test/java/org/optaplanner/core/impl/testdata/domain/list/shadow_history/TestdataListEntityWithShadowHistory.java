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

package org.optaplanner.core.impl.testdata.domain.list.shadow_history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

@PlanningEntity
public class TestdataListEntityWithShadowHistory extends TestdataObject {

    public static EntityDescriptor<TestdataListSolutionWithShadowHistory> buildEntityDescriptor() {
        return TestdataListSolutionWithShadowHistory.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataListEntityWithShadowHistory.class);
    }

    public static ListVariableDescriptor<TestdataListSolutionWithShadowHistory> buildVariableDescriptorForValueList() {
        return (ListVariableDescriptor<TestdataListSolutionWithShadowHistory>) buildEntityDescriptor()
                .getGenuineVariableDescriptor("valueList");
    }

    public static TestdataListEntityWithShadowHistory createWithValues(String code,
            TestdataListValueWithShadowHistory... values) {
        // Set up shadow variables to preserve consistency.
        return new TestdataListEntityWithShadowHistory(code, values).setUpShadowVariables();
    }

    TestdataListEntityWithShadowHistory setUpShadowVariables() {
        for (int i = 0; i < valueList.size(); i++) {
            TestdataListValueWithShadowHistory value = valueList.get(i);
            value.setEntity(this);
            value.setIndex(i);
            if (i > 0) {
                value.setPrevious(valueList.get(i - 1));
            }
            if (i < valueList.size() - 1) {
                value.setNext(valueList.get(i + 1));
            }
        }
        return this;
    }

    @PlanningListVariable(valueRangeProviderRefs = "valueRange")
    private List<TestdataListValueWithShadowHistory> valueList;

    public TestdataListEntityWithShadowHistory() {
    }

    public TestdataListEntityWithShadowHistory(String code, List<TestdataListValueWithShadowHistory> valueList) {
        super(code);
        this.valueList = valueList;
    }

    public TestdataListEntityWithShadowHistory(String code, TestdataListValueWithShadowHistory... values) {
        this(code, new ArrayList<>(Arrays.asList(values)));
    }

    public List<TestdataListValueWithShadowHistory> getValueList() {
        return valueList;
    }
}
