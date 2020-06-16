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

package org.optaplanner.core.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public final class Util {

    public static void assertEquals(boolean x, boolean y) {
        if (x) {
            assertThat(y).isTrue();
        } else {
            assertThat(y).isFalse();
        }
    }

    public static void assertArrayEquals(String[] array, String[] array2) {
        assertThat(array2).isEqualTo(array);
    }

    public static void assertTrue(boolean b) {
        assertThat(b).isTrue();
    }

    public static void assertFalse(boolean b) {
        assertThat(b).isFalse();
    }

    public static <T extends Object> void assertSame(T s, T toString) {
        assertThat(toString).isSameAs(s);
    }

    public static <T extends Object> void assertNotSame(T s, T toString) {
        assertThat(toString).isNotSameAs(s);
    }

    public static <T extends Object> void assertEquals(T s, T toString) {
        assertThat(toString).isEqualTo(s);
    }

    public static void assertEquals(long s, long toString) {
        assertThat(toString).isEqualTo(s);
    }

    public static void assertEquals(int s, int toString) {
        assertThat(toString).isEqualTo(s);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertThat(actual).isEqualTo(expected, offset(delta));
    }

    public static <T extends Object> void assertNotEquals(T s, T toString) {
        assertThat(toString).isNotEqualTo(s);
    }

    public static void assertNotEquals(int s, int toString) {
        assertThat(toString).isNotEqualTo(s);
    }

    public static <T extends Object> void assertNull(T s) {
        assertThat(s).isNull();
    }

    public static <T extends Object> void assertNotNull(T s) {
        assertThat(s).isNotNull();
    }

    public static void assertEquals(int i, long size) {
        assertThat(size).isEqualTo(i);
    }

}
