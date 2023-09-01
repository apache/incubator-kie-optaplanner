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

package org.optaplanner.core.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.util.ListBasedScalingOrderedSet.LIST_SIZE_THRESHOLD;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ListBasedScalingOrderedSetTest {

    @Test
    void addRemoveAroundThreshold() {
        Set<String> set = new ListBasedScalingOrderedSet<>();
        assertThat(set.add("s1")).isTrue();
        assertThat(set.add("s1")).isFalse();
        assertThat(set.add("s2")).isTrue();
        assertThat(set.add("s1")).isFalse();
        assertThat(set.add("s2")).isFalse();
        assertThat(set.remove("s2")).isTrue();
        assertThat(set.remove("s2")).isFalse();
        assertThat(set.add("s2")).isTrue();
        assertThat(set)
                .hasSize(2)
                .containsExactlyInAnyOrder("s1", "s2");

        for (int i = 0; i < LIST_SIZE_THRESHOLD - 3; i++) {
            set.add("filler " + i);
        }
        assertThat(set.add("s2")).isFalse();
        assertThat(set.add("s3")).isTrue();
        assertThat(set.add("s2")).isFalse();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD);
        assertThat(set.add("s4")).isTrue();
        assertThat(set.add("s2")).isFalse();
        assertThat(set.add("s3")).isFalse();
        assertThat(set.add("s4")).isFalse();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD + 1);
        assertThat(set.remove("s4")).isTrue();
        assertThat(set.add("s2")).isFalse();
        assertThat(set.add("s3")).isFalse();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD);
        assertThat(set.add("s5")).isTrue();
        assertThat(set.add("s2")).isFalse();
        assertThat(set.add("s3")).isFalse();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD + 1);
        assertThat(set.add("s6")).isTrue();
        assertThat(set.add("s2")).isFalse();
        assertThat(set.add("s3")).isFalse();
        assertThat(set)
                .hasSize(LIST_SIZE_THRESHOLD + 2)
                .contains("s1", "s2", "s3", "s5", "s6")
                .doesNotContain("s4");
    }

    @Test
    void addAllAroundThreshold() {
        Set<String> set = new ListBasedScalingOrderedSet<>();
        assertThat(set.addAll(Arrays.asList("s1", "s2", "s3"))).isTrue();
        assertThat(set).hasSize(3);
        assertThat(set.addAll(Arrays.asList("s1", "s3", "s4", "s5"))).isTrue();
        assertThat(set.addAll(Arrays.asList("s1", "s2", "s4"))).isFalse();
        assertThat(set)
                .hasSize(5)
                .containsExactlyInAnyOrder("s1", "s2", "s3", "s4", "s5");

        for (int i = 0; i < LIST_SIZE_THRESHOLD - 7; i++) {
            set.add("filler " + i);
        }
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD - 2);
        assertThat(set.addAll(Arrays.asList("s6", "s7", "s2", "s3", "s8", "s9"))).isTrue();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD + 2);
        assertThat(set.remove("s1")).isTrue();
        assertThat(set.remove("s5")).isTrue();
        assertThat(set).hasSize(LIST_SIZE_THRESHOLD);
        assertThat(set.addAll(Arrays.asList("s1", "s2", "s10"))).isTrue();
        assertThat(set)
                .hasSize(LIST_SIZE_THRESHOLD + 2)
                .contains("s1", "s2", "s3", "s4", "s6", "s7", "s8", "s9", "s10")
                .doesNotContain("s5");
    }

}
