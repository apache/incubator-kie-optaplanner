/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.Random;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EmptyValueRangeTest {

    @Test
    public void getSize() {
        assertThat(new EmptyValueRange<Integer>().getSize()).isEqualTo(0L);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get() {
        new EmptyValueRange<Integer>().get(0L);
    }

    @Test
    public void contains() {
        assertThat(new EmptyValueRange<Integer>().contains(5)).isEqualTo(false);
        assertThat(new EmptyValueRange<Integer>().contains(null)).isEqualTo(false);
    }

    @Test
    public void createOriginalIterator() {
        assertAllElementsOfIterator(new EmptyValueRange<Integer>().createOriginalIterator());
    }

    @Test
    public void createRandomIterator() {
        Random workingRandom = mock(Random.class);
        assertElementsOfIterator(new EmptyValueRange<Integer>().createRandomIterator(workingRandom));
    }

}
