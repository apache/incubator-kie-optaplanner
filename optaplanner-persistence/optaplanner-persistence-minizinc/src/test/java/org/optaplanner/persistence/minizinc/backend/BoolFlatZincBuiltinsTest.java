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

public class BoolFlatZincBuiltinsTest {

    final ConstraintVerifier<BoolFlatZincBuiltinsTestConstraintProvider, MyPlanningSolution> constraintVerifier =
            ConstraintVerifier
                    .build(new BoolFlatZincBuiltinsTestConstraintProvider(), MyPlanningSolution.class,
                            MyIntVariable.class,
                            MyBoolVariable.class,
                            MySecondBoolVariable.class,
                            ConstrainedBoolVariable.class,
                            MyBoolArrayVariable.class,
                            MySecondBoolArrayVariable.class);

    @Test
    public void test_array_bool_and() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_array_bool_element() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(0),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(0),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_element)
                .given(new MyIntVariable(3),
                        new MyBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_array_bool_or() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_or)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_or)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_or)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_or)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_array_bool_xor() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_xor)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_xor)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_xor)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_xor)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true))
                .penalizes(1);
    }

    @Test
    public void test_array_var_bool_element() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(0),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(0),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(1),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(1),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(2),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, false),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_var_bool_element)
                .given(new MyIntVariable(2),
                        new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, false),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_bool2int() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(false),
                        new MyIntVariable(0))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(false),
                        new MyIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(true),
                        new MyIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(true),
                        new MyIntVariable(0))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(false),
                        new MyIntVariable(2))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(true),
                        new MyIntVariable(2))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(false),
                        new MyIntVariable(-1))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool2int)
                .given(new MyBoolVariable(true),
                        new MyIntVariable(-1))
                .penalizes(1);
    }

    @Test
    public void test_bool_and() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_and)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_bool_clause() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_clause)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MySecondBoolArrayVariable(0, false),
                        new MySecondBoolArrayVariable(1, false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_clause)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MySecondBoolArrayVariable(0, false),
                        new MySecondBoolArrayVariable(1, true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_clause)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false),
                        new MySecondBoolArrayVariable(0, false),
                        new MySecondBoolArrayVariable(1, false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_clause)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false),
                        new MySecondBoolArrayVariable(0, false),
                        new MySecondBoolArrayVariable(1, true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_clause)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MySecondBoolArrayVariable(0, true),
                        new MySecondBoolArrayVariable(1, true))
                .penalizes(0);
    }

    @Test
    public void test_bool_eq() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_bool_eq_reif() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_eq_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_bool_le() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_bool_le_reif() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_le_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_bool_lin_eq() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyIntVariable(6))
                .penalizes(0);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyIntVariable(5))
                .penalizes(1);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true),
                        new MyIntVariable(7))
                .penalizes(1);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, true),
                        new MyIntVariable(4))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, true),
                        new MyIntVariable(5))
                .penalizes(1);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, false),
                        new MyIntVariable(0))
                .penalizes(0);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_eq)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, false),
                        new MyIntVariable(1))
                .penalizes(1);
    }

    @Test
    public void test_bool_lin_le() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(1, true),
                        new MyBoolArrayVariable(2, false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lin_le)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(1, false),
                        new MyBoolArrayVariable(2, false))
                .penalizes(0);
    }

    @Test
    public void test_bool_lt() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
    }

    @Test
    public void test_bool_lt_reif() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_lt_reif)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_bool_not() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_not)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_not)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_not)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_not)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
    }

    @Test
    public void test_bool_or() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_or)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
    }

    @Test
    public void test_bool_xor_3_args() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(false),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_3args)
                .given(new MyBoolVariable(true),
                        new MySecondBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_bool_xor_2args() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_2args)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_2args)
                .given(new MyBoolVariable(false),
                        new ConstrainedBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_2args)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::bool_xor_2args)
                .given(new MyBoolVariable(true),
                        new ConstrainedBoolVariable(true))
                .penalizes(1);
    }
}
