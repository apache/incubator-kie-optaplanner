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

public class IntFlatZincBuiltinsTest {

    final ConstraintVerifier<IntFlatZincBuiltinsTestConstraintProvider, MyPlanningSolution> constraintVerifier =
            ConstraintVerifier
                    .build(new IntFlatZincBuiltinsTestConstraintProvider(), MyPlanningSolution.class,
                            MyIntVariable.class,
                            MySecondIntVariable.class,
                            ConstrainedIntVariable.class,
                            MyIntArrayVariable.class,
                            MyBoolVariable.class);

    @Test
    public void test_array_int_element() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(0), new ConstrainedIntVariable(0))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(0), new ConstrainedIntVariable(10))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(1), new ConstrainedIntVariable(10))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(1), new ConstrainedIntVariable(20))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(2), new ConstrainedIntVariable(20))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_element)
                .given(new MyIntVariable(2), new ConstrainedIntVariable(30))
                .penalizes(0);
    }

    @Test
    public void test_array_int_maximum() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_maximum)
                .given(new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_maximum)
                .given(new ConstrainedIntVariable(30),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
    }

    @Test
    public void test_array_int_minimum() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_minimum)
                .given(new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_int_minimum)
                .given(new ConstrainedIntVariable(30),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
    }

    @Test
    public void test_array_var_int_element() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(0),
                        new ConstrainedIntVariable(0),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(0),
                        new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(10),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::array_var_int_element)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(20),
                        new MyIntArrayVariable(0, 10),
                        new MyIntArrayVariable(1, 20),
                        new MyIntArrayVariable(2, 30))
                .penalizes(0);
    }

    @Test
    public void test_int_abs() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(10),
                        new ConstrainedIntVariable(-10))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(10),
                        new ConstrainedIntVariable(10))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_abs)
                .given(new MyIntVariable(-10),
                        new ConstrainedIntVariable(10))
                .penalizes(0);
    }

    @Test
    public void test_int_div() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_div)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_div)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_div)
                .given(new MyIntVariable(3),
                        new MySecondIntVariable(2),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
    }

    @Test
    public void test_int_eq() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq)
                .given(new MyIntVariable(-5),
                        new ConstrainedIntVariable(-5))
                .penalizes(0);
    }

    @Test
    public void test_int_eq_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_eq_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_int_le() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
    }

    @Test
    public void test_int_le_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_le_reif)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(1);
    }

    @Test
    public void test_int_lin_eq() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2))
                .penalizes(1);
    }

    @Test
    public void test_int_lin_eq_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_eq_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_int_lin_le() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le)
                .given(new MyIntArrayVariable(0, 2),
                        new MyIntArrayVariable(1, 1))
                .penalizes(1);
    }

    @Test
    public void test_int_lin_le_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 2),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_le_reif)
                .given(new MyIntArrayVariable(0, 2),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_int_lin_ne() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2))
                .penalizes(0);
    }

    @Test
    public void test_int_lin_ne_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lin_ne_reif)
                .given(new MyIntArrayVariable(0, 1),
                        new MyIntArrayVariable(1, 2),
                        new MyBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_int_lt() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
    }

    @Test
    public void test_int_lt_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_lt_reif)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(1);
    }

    @Test
    public void test_int_max() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_max)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(5))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_max)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(10))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_max)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(12))
                .penalizes(1);
    }

    @Test
    public void test_int_min() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_min)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(5))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_min)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(10))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_min)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(2))
                .penalizes(1);
    }

    @Test
    public void test_int_mod() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_mod)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_mod)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(0))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_mod)
                .given(new MyIntVariable(3),
                        new MySecondIntVariable(2),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_mod)
                .given(new MyIntVariable(3),
                        new MySecondIntVariable(2),
                        new ConstrainedIntVariable(5))
                .penalizes(1);
    }

    @Test
    public void test_int_ne() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne)
                .given(new MyIntVariable(2),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne)
                .given(new MyIntVariable(-5),
                        new ConstrainedIntVariable(-5))
                .penalizes(1);
    }

    @Test
    public void test_int_ne_reif() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_ne_reif)
                .given(new MyIntVariable(1),
                        new ConstrainedIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_int_plus() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_plus)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_plus)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(15))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_plus)
                .given(new MyIntVariable(3),
                        new MySecondIntVariable(-2),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
    }

    @Test
    public void test_int_pow() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_pow)
                .given(new MyIntVariable(2),
                        new MySecondIntVariable(3),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_pow)
                .given(new MyIntVariable(2),
                        new MySecondIntVariable(3),
                        new ConstrainedIntVariable(8))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_pow)
                .given(new MyIntVariable(5),
                        new MySecondIntVariable(2),
                        new ConstrainedIntVariable(25))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_pow)
                .given(new MyIntVariable(16),
                        new MySecondIntVariable(0),
                        new ConstrainedIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_pow)
                .given(new MyIntVariable(16),
                        new MySecondIntVariable(0),
                        new ConstrainedIntVariable(8))
                .penalizes(1);
    }

    @Test
    public void test_int_times() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_times)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_times)
                .given(new MyIntVariable(10),
                        new MySecondIntVariable(5),
                        new ConstrainedIntVariable(50))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::int_times)
                .given(new MyIntVariable(3),
                        new MySecondIntVariable(-2),
                        new ConstrainedIntVariable(-6))
                .penalizes(0);
    }

    @Test
    public void test_set_in() {
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(0))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(4))
                .penalizes(1);
        constraintVerifier.verifyThat(IntFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(5))
                .penalizes(0);
    }
}
