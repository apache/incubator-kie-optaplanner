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

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertCodesOfIterator;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntFunction;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class RandomListChangeIteratorTest {

    @Test
    void iterator() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity a = new TestdataListEntity("A", v1, v2);
        TestdataListEntity b = new TestdataListEntity("B");
        TestdataListEntity c = new TestdataListEntity("C", v3);

        Random random = mock(Random.class);
        when(random.nextInt(3)).thenReturn(0, 1, 2); // global source indexes
        when(random.nextInt(6)).thenReturn(2, 3, 0); // global destination indexes

        RandomListChangeIterator<TestdataListSolution> randomListChangeIterator = new RandomListChangeIterator<>(
                TestdataListEntity.buildVariableDescriptorForValueList(),
                Arrays.asList(a, b, c),
                random);

        // 0 unfolds to [A, 0]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoSourceEntityAndListIndex, 0, a, 0);
        // 1 unfolds to [A, 1]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoSourceEntityAndListIndex, 1, a, 1);
        // 2 unfolds to [C, 0]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoSourceEntityAndListIndex, 2, c, 0);

        // 0 unfolds to [A, 0]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoDestinationEntityAndListIndex, 0, a, 0);
        // 1 unfolds to [A, 1]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoDestinationEntityAndListIndex, 1, a, 1);
        // 2 unfolds to [A, 2]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoDestinationEntityAndListIndex, 2, a, 2);
        // 3 unfolds to [B, 0]
        assertEntityAndIndex(randomListChangeIterator::unfoldGlobalIndexIntoDestinationEntityAndListIndex, 3, b, 0);

        assertCodesOfIterator(randomListChangeIterator,
                "1 {A[0]->A[2]}",
                "2 {A[1]->B[0]}",
                "3 {C[0]->A[0]}");
    }

    private static void assertEntityAndIndex(
            IntFunction<Pair<Object, Integer>> unfoldMethod,
            int globalIndex,
            Object expectedEntity,
            int expectedListIndex) {
        Pair<Object, Integer> pair = unfoldMethod.apply(globalIndex);
        assertThat(pair.getLeft()).isEqualTo(expectedEntity);
        assertThat(pair.getRight()).isEqualTo(expectedListIndex);
    }
}
