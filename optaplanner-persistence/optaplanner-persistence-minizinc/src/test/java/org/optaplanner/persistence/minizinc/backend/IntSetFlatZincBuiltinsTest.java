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

public class IntSetFlatZincBuiltinsTest {

    final ConstraintVerifier<IntSetFlatZincBuiltinsTestConstraintProvider, MyPlanningSolution> constraintVerifier =
            ConstraintVerifier
                    .build(new IntSetFlatZincBuiltinsTestConstraintProvider(), MyPlanningSolution.class,
                            MyIntVariable.class,
                            MyBoolVariable.class,
                            MyIntSetVariable.class,
                            MySecondIntSetVariable.class,
                            ConstrainedIntSetVariable.class,
                            MyIntSetArrayVariable.class);

    @Test
    public void test_array_set_element() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(0), new ConstrainedIntSetVariable())
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(0), new ConstrainedIntSetVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(1), new ConstrainedIntSetVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(1), new ConstrainedIntSetVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(2), new ConstrainedIntSetVariable(2))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(2), new ConstrainedIntSetVariable(3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_set_element)
                .given(new MyIntVariable(2), new ConstrainedIntSetVariable(1, 3))
                .penalizes(0);
    }

    @Test
    public void test_array_var_set_element() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(0),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 2),
                        new ConstrainedIntSetVariable())
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(0),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 2),
                        new ConstrainedIntSetVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(1),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 2),
                        new ConstrainedIntSetVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(1),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 2),
                        new ConstrainedIntSetVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(1),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 1, 3),
                        new ConstrainedIntSetVariable(3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::array_var_set_element)
                .given(new MyIntVariable(1),
                        new MyIntSetArrayVariable(0, 1),
                        new MyIntSetArrayVariable(1, 1, 3),
                        new ConstrainedIntSetVariable(1, 3))
                .penalizes(0);
    }

    @Test
    public void test_set_card() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(),
                        new MyIntVariable(0))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(),
                        new MyIntVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(),
                        new MyIntVariable(-1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(3),
                        new MyIntVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(3),
                        new MyIntVariable(3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_card)
                .given(new ConstrainedIntSetVariable(3),
                        new MyIntVariable(2))
                .penalizes(1);
    }

    @Test
    public void test_set_diff() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_diff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_diff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_diff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_diff)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable())
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_diff)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(3))
                .penalizes(1);
    }

    @Test
    public void test_set_eq() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6))
                .penalizes(1);
    }

    @Test
    public void test_set_eq_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_eq_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_in() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(0),
                        new ConstrainedIntSetVariable(1, 3, 5))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(1),
                        new ConstrainedIntSetVariable(1, 3, 5))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(3),
                        new ConstrainedIntSetVariable(1, 3, 5))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in)
                .given(new MyIntVariable(7),
                        new ConstrainedIntSetVariable(1, 3, 5))
                .penalizes(1);
    }

    @Test
    public void test_set_in_reif_const() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(-1),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(0),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(3),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(-1),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(0),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(2),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_const)
                .given(new MyIntVariable(3),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_in_reif_var() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(0),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(1),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(3),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(7),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(0),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(1),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(3),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_in_reif_var)
                .given(new MyIntVariable(7),
                        new ConstrainedIntSetVariable(1, 3, 5),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_intersect() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_intersect)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_intersect)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_intersect)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_intersect)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(3, 4),
                        new ConstrainedIntSetVariable())
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_intersect)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(3))
                .penalizes(1);
    }

    @Test
    public void test_set_le() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable())
                .penalizes(1);
    }

    @Test
    public void test_set_le_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_le_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_lt() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable())
                .penalizes(1);
    }

    @Test
    public void test_set_lt_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_lt_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_ne() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6))
                .penalizes(0);
    }

    @Test
    public void test_set_ne_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6),
                        new MyBoolVariable(true))
                .penalizes(0);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_ne_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(4, 5, 6),
                        new MyBoolVariable(false))
                .penalizes(1);
    }

    @Test
    public void test_set_subset() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(1, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(1, 2, 3, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
    }

    @Test
    public void test_set_subset_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 2, 3, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(1, 2, 3, 4),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_subset_reif)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_superset() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable())
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3, 4))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
    }

    @Test
    public void test_set_superset_reif() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 3),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3, 4),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(true))
                .penalizes(1);

        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 3),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(),
                        new MyBoolVariable(false))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 4),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(1, 2, 3, 4),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_superset_reif)
                .given(new MyIntSetVariable(5),
                        new ConstrainedIntSetVariable(1, 2, 3),
                        new MyBoolVariable(false))
                .penalizes(0);
    }

    @Test
    public void test_set_symdiff() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 3, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3, 4))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(3, 4),
                        new ConstrainedIntSetVariable(1, 2, 3, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable(3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(3))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_symdiff)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(1, 2),
                        new ConstrainedIntSetVariable(1, 2))
                .penalizes(1);
    }

    @Test
    public void test_set_union() {
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_union)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_union)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_union)
                .given(new MyIntSetVariable(1, 2, 3),
                        new MySecondIntSetVariable(2, 4),
                        new ConstrainedIntSetVariable(1, 2, 3))
                .penalizes(1);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_union)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(3, 4),
                        new ConstrainedIntSetVariable(1, 2, 3, 4))
                .penalizes(0);
        constraintVerifier.verifyThat(IntSetFlatZincBuiltinsTestConstraintProvider::set_union)
                .given(new MyIntSetVariable(1, 2),
                        new MySecondIntSetVariable(1, 2, 3),
                        new ConstrainedIntSetVariable())
                .penalizes(1);
    }
}
