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

package org.optaplanner.examples.common.business;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

public class AlphaNumericStringComparatorTest {

    @Test
    public void compare() {
        AlphaNumericStringComparator comparator = new AlphaNumericStringComparator();
        assertCompareEquals(comparator, "aaa", "aaa");
        assertCompareLower(comparator, "aaa", "aaaa");
        assertCompareLower(comparator, "aaa", "aba");
        assertCompareLower(comparator, "aaa", "ba");

        assertCompareEquals(comparator, "a1", "a1");
        assertCompareEquals(comparator, "a123", "a123");
        assertCompareLower(comparator, "a1", "a2");
        assertCompareLower(comparator, "a2", "a10");
        assertCompareLower(comparator, "a99", "a100");
        assertCompareLower(comparator, "2", "10");
        assertCompareLower(comparator, "2a", "10a");
        assertCompareLower(comparator, "a-2", "a-10");
        assertCompareLower(comparator, "a-2.5", "a-10.0");
        assertCompareLower(comparator, "a-0.5", "a-0.6");
    }

    public <T> void assertCompareEquals(Comparator<T> comparator, T a, T b) {
        assertSoftly(softly -> {
            softly.assertThat(a).usingComparator(comparator).isEqualTo(b);
            softly.assertThat(b).usingComparator(comparator).isEqualTo(a);
        });
    }

    public <T> void assertCompareLower(Comparator<T> comparator, T a, T b) {
        assertSoftly(softly -> {
            softly.assertThat(comparator.compare(a, b)).isEqualTo(-1);
            softly.assertThat(comparator.compare(b, a)).isEqualTo(1);
        });
    }

}
