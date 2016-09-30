/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.config.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.config.util.ConfigUtils.*;

public class ConfigUtilsTest {

    @Test
    public void testMergeProperty() {
        Integer a = null;
        Integer b = null;
        assertThat(mergeProperty(a, b)).isNull();
        a = Integer.valueOf(1);
        assertThat(mergeProperty(a, b)).isNull();
        b = Integer.valueOf(10);
        assertThat(mergeProperty(a, b)).isNull();
        b = Integer.valueOf(1);
        assertThat(mergeProperty(a, b)).isEqualTo(Integer.valueOf(1));
        a = null;
        assertThat(mergeProperty(a, b)).isNull();
    }

    @Test
    public void testMeldProperty() {
        Integer a = null;
        Integer b = null;
        assertThat(meldProperty(a, b)).isNull();
        a = Integer.valueOf(1);
        assertThat(meldProperty(a, b)).isEqualTo(Integer.valueOf(1));
        b = Integer.valueOf(10);
        assertThat(meldProperty(a, b)).isEqualTo(mergeProperty(Integer.valueOf(1), Integer.valueOf(10)));
        a = null;
        assertThat(meldProperty(a, b)).isEqualTo(Integer.valueOf(10));
    }

    @Test
    public void testCeilDivide() {
        assertThat(ceilDivide(19, 2)).isEqualTo(10);
        assertThat(ceilDivide(20, 2)).isEqualTo(10);
        assertThat(ceilDivide(21, 2)).isEqualTo(11);

        assertThat(ceilDivide(19, -2)).isEqualTo(-9);
        assertThat(ceilDivide(20, -2)).isEqualTo(-10);
        assertThat(ceilDivide(21, -2)).isEqualTo(-10);

        assertThat(ceilDivide(-19, 2)).isEqualTo(-9);
        assertThat(ceilDivide(-20, 2)).isEqualTo(-10);
        assertThat(ceilDivide(-21, 2)).isEqualTo(-10);

        assertThat(ceilDivide(-19, -2)).isEqualTo(10);
        assertThat(ceilDivide(-20, -2)).isEqualTo(10);
        assertThat(ceilDivide(-21, -2)).isEqualTo(11);
    }

    @Test(expected = ArithmeticException.class)
    public void testCeilDivideByZero() {
        ceilDivide(20, -0);
    }

}
