/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.common.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.reflect.field.TestdataFieldAnnotatedEntity;

public class ReflectionFieldMemberAccessorTest {

    @Test
    public void fieldAnnotatedEntity() throws NoSuchFieldException {
        ReflectionFieldMemberAccessor memberAccessor = new ReflectionFieldMemberAccessor(
                TestdataFieldAnnotatedEntity.class.getDeclaredField("value"));
        assertEquals("value", memberAccessor.getName());
        assertEquals(TestdataValue.class, memberAccessor.getType());
        assertEquals(true, memberAccessor.isAnnotationPresent(PlanningVariable.class));

        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataFieldAnnotatedEntity e1 = new TestdataFieldAnnotatedEntity("e1", v1);
        assertSame(v1, memberAccessor.executeGetter(e1));
        memberAccessor.executeSetter(e1, v2);
        assertSame(v2, e1.getValue());
    }

}
