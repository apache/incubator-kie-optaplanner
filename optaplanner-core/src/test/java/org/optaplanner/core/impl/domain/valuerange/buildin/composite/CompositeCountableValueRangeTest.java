/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.valuerange.buildin.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.optaplanner.core.impl.domain.valuerange.buildin.collection.ListValueRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class CompositeCountableValueRangeTest {

    private <T> CompositeCountableValueRange<T> createValueRange(List<T>... lists) {
        List<ListValueRange<T>> childValueRangeList = new ArrayList<>(lists.length);
        for (List<T> list : lists) {
            childValueRangeList.add(new ListValueRange<>(list));
        }
        return new CompositeCountableValueRange<>(childValueRangeList);
    }

    @Test
    public void getSize() {
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).getSize()).isEqualTo(7L);
        assertThat(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d")).getSize()).isEqualTo(4L);
        assertThat(createValueRange(Collections.<String>emptyList(), Collections.<String>emptyList()).getSize()).isEqualTo(0L);
    }

    @Test
    public void get() {
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).get(2L).intValue()).isEqualTo(5);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).get(4L).intValue()).isEqualTo(-15);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).get(6L).intValue()).isEqualTo(-1);
        assertThat(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d")).get(2L)).isEqualTo("c");
    }

    @Test
    public void contains() {
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(5)).isEqualTo(true);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(4)).isEqualTo(false);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(-15)).isEqualTo(true);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(-14)).isEqualTo(false);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(-1)).isEqualTo(true);
        assertThat(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1)).contains(1)).isEqualTo(false);
        assertThat(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d")).contains("c")).isEqualTo(true);
        assertThat(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d")).contains("n")).isEqualTo(false);
    }

    @Test
    public void createOriginalIterator() {
        assertAllElementsOfIterator(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1))
                .createOriginalIterator(), 0, 2, 5, 10, -15, 25, -1);
        assertAllElementsOfIterator(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d"))
                .createOriginalIterator(), "a", "b", "c", "d");
        assertAllElementsOfIterator(createValueRange(Collections.<String>emptyList(), Collections.<String>emptyList())
                .createOriginalIterator());
    }

    @Test
    public void createRandomIterator() {
        Random workingRandom = mock(Random.class);

        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(createValueRange(Arrays.asList(0, 2, 5, 10), Arrays.asList(-15, 25, -1))
                .createRandomIterator(workingRandom), 10, 0);
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(createValueRange(Arrays.asList("a", "b"), Arrays.asList("c"), Arrays.asList("d"))
                .createRandomIterator(workingRandom), "d", "a");
        assertElementsOfIterator(createValueRange(Collections.<String>emptyList(), Collections.<String>emptyList())
                .createRandomIterator(workingRandom));
    }

}
