/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.minizinc.backend;

import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class FlatZincBuiltinsTest {

    final ConstraintVerifier<FlatZincBuiltinsTestConstraintProvider, MyPlanningSolution> constraintVerifier = ConstraintVerifier
            .build(new FlatZincBuiltinsTestConstraintProvider(), MyPlanningSolution.class, MyIntVariable.class,
                    ConstrainedIntVariable.class,
                    MyIntArrayVariable.class);

    @Test
    public void test_array_int_element() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(0), new ConstrainedIntVariable(0))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(0), new ConstrainedIntVariable(10))
                .penalizes(0);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(1), new ConstrainedIntVariable(10))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(1), new ConstrainedIntVariable(20))
                .penalizes(0);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(2), new ConstrainedIntVariable(20))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(2), new ConstrainedIntVariable(30))
                .penalizes(0);
    }

    @Test
    public void test_array_int_maximum() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_maximum)
                .given(new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_maximum)
                .given(new ConstrainedIntVariable(30),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
    }

    @Test
    public void test_array_int_minimum() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_minimum)
                .given(new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_int_minimum)
                .given(new ConstrainedIntVariable(30),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
    }

    @Test
    public void test_array_var_int_element() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(0),
                        new ConstrainedIntVariable(0),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(0),
                        new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(20),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
    }

    @Test
    public void test_int_abs() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(10),
                        new ConstrainedIntVariable(-10))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(10),
                        new ConstrainedIntVariable(10))
                .penalizes(0);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(-10),
                        new ConstrainedIntVariable(10))
                .penalizes(0);
    }

    @Test
    public void test_int_lin_ne() {
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::int_lin_ne)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1))
                .penalizes(1);
        constraintVerifier.verifyThat(FlatZincBuiltinsTestConstraintProvider::int_lin_ne)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2))
                .penalizes(0);
    }
}
