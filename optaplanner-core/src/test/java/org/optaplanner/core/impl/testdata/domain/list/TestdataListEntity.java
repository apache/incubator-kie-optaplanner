/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.testdata.domain.list;

import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.DefaultListVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

@PlanningEntity
public class TestdataListEntity extends TestdataObject {

    public static <Solution_> DefaultListVariableDescriptor<Solution_> buildVariableDescriptorForValueList() {
        MemberAccessor memberAccessor = null;
        try {
            memberAccessor = MemberAccessorFactory.buildMemberAccessor(
                    TestdataListEntity.class.getDeclaredField("valueList"),
                    MemberAccessorFactory.MemberAccessorType.FIELD_OR_GETTER_METHOD,
                    null, // @PlanningCollectionVariable
                    DomainAccessType.REFLECTION,
                    Collections.emptyMap());
        } catch (NoSuchFieldException e) {
            fail("No such field", e);
        }
        return new DefaultListVariableDescriptor<>(null, memberAccessor);
    }

    // TODO @PlanningCollectionVariable
    private final List<TestdataValue> valueList;

    public TestdataListEntity(String code, List<TestdataValue> valueList) {
        super(code);
        this.valueList = valueList;
    }

    public TestdataListEntity(String code, TestdataValue... values) {
        this(code, new ArrayList<>(Arrays.asList(values)));
    }

    public List<TestdataValue> getValueList() {
        return valueList;
    }
}
