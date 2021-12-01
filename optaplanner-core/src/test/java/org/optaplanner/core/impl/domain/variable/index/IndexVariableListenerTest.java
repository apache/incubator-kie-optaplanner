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

package org.optaplanner.core.impl.domain.variable.index;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class IndexVariableListenerTest {

    @Test
    void index() {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);

        IndexVariableListener<TestdataListSolution> indexVariableListener = new IndexVariableListener<>(
                TestdataListValue.buildVariableDescriptorForIndex(),
                TestdataListEntity.buildVariableDescriptorForValueList());

        TestdataListValue v1 = new TestdataListValue("v1");
        TestdataListValue v2 = new TestdataListValue("v2");
        TestdataListValue v3 = new TestdataListValue("v3");
        TestdataListEntity entity = new TestdataListEntity("A", v1, v2, v3);

        assertNullIndexes(indexVariableListener, v1, v2, v3);

        // When an entity is added,
        indexVariableListener.beforeEntityAdded(scoreDirector, entity);
        indexVariableListener.afterEntityAdded(scoreDirector, entity);
        // the shadow variables of its planning values are set.
        assertIndexes(indexVariableListener, v1, v2, v3);

        // Insert a value at index 2: A[v1, v2, 3] => A[v1, v2, v4, v3].
        //                                                      ^
        TestdataListValue v4 = new TestdataListValue("v4");
        indexVariableListener.beforeVariableChanged(scoreDirector, entity, 2);
        entity.getValueList().add(2, v4);
        indexVariableListener.afterVariableChanged(scoreDirector, entity, 2);
        assertIndexes(indexVariableListener, v1, v2, v4, v3);

        // Append a value: A[v1, v2, v4, v3] => A[v1, v2, v4, v3, v5].
        //                                                        ^
        TestdataListValue v5 = new TestdataListValue("v5");
        indexVariableListener.beforeVariableChanged(scoreDirector, entity, 4);
        entity.getValueList().add(4, v5);
        indexVariableListener.afterVariableChanged(scoreDirector, entity, 4);
        assertIndexes(indexVariableListener, v1, v2, v4, v3, v5);

        // Remove a value from index 0: A[v1, v2, v4, v3, v5] => A[v2, v4, v3, v5]
        //                                ^
        indexVariableListener.beforeVariableChanged(scoreDirector, entity, 0);
        entity.getValueList().remove(v1);
        indexVariableListener.afterVariableChanged(scoreDirector, entity, 0);
        assertIndexes(indexVariableListener, v2, v4, v3, v5);
        assertNullIndexes(indexVariableListener, v1);

        // Remove a value from the end: A[v2, v4, v3, v5] => A[v2, v4, v3]
        //                                            ^
        indexVariableListener.beforeVariableChanged(scoreDirector, entity, 3);
        entity.getValueList().remove(v5);
        indexVariableListener.afterVariableChanged(scoreDirector, entity, 3);
        assertIndexes(indexVariableListener, v2, v4, v3);
        assertNullIndexes(indexVariableListener, v1, v5);
    }

    @Test
    void removeEntity() {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);

        IndexVariableListener<TestdataListSolution> indexVariableListener = new IndexVariableListener<>(
                TestdataListValue.buildVariableDescriptorForIndex(),
                TestdataListEntity.buildVariableDescriptorForValueList());

        TestdataListValue v1 = new TestdataListValue("v1");
        TestdataListValue v2 = new TestdataListValue("v2");
        TestdataListValue v3 = new TestdataListValue("v3");
        TestdataListEntity entity = TestdataListEntity.createWithValues("A", v1, v2, v3);

        assertIndexes(indexVariableListener, v1, v2, v3);

        indexVariableListener.beforeEntityRemoved(scoreDirector, entity);
        indexVariableListener.afterEntityRemoved(scoreDirector, entity);

        // TODO after removing
        // Proposal A: all shadow variables should be unset
        assertNullIndexes(indexVariableListener, v1, v2, v3);
        // Proposal B: elements are still in the list so their shadow variables are consistent and don't need to be unset
        //        assertIndexes(v1, v2, v3);

        // Winner: Proposal A because removing an entity != removing all of its values. The values are still in the value range
        // and so they NEED to be made UNASSIGNED when the entity is removed => Yes, UNSET inverse+index shadow variables.
    }

    private static void assertIndexes(IndexVariableListener<?> indexVariableListener, TestdataListValue... values) {
        SoftAssertions.assertSoftly(softly -> IntStream.range(0, values.length).forEach(i -> {
            softly.assertThat(values[i].getIndex())
                    .as("Index of " + values[i] + " in " + Arrays.toString(values))
                    .isEqualTo(i);
            softly.assertThat(indexVariableListener.getIndex(values[i]))
                    .as("Index of " + values[i] + " in " + Arrays.toString(values))
                    .isEqualTo(i);
        }));
    }

    private static void assertNullIndexes(IndexVariableListener<?> indexVariableListener, TestdataListValue... values) {
        SoftAssertions.assertSoftly(softly -> {
            for (TestdataListValue value : values) {
                softly.assertThat(value.getIndex()).isNull();
                softly.assertThat(indexVariableListener.getIndex(value)).isNull();
            }
        });
    }
}
