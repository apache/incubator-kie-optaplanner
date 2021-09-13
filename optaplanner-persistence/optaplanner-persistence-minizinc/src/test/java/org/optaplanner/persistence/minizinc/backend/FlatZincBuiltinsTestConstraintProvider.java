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

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class FlatZincBuiltinsTestConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // intentionally empty; each constraint is tested individually
        };
    }

    public Constraint array_int_element(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.array_int_element(MyIntVariable.class, new int[] { 10, 20, 30 }, ConstrainedIntVariable.class,
                "array_int_element", constraintFactory);
    }

    public Constraint array_int_maximum(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.array_int_maximum(ConstrainedIntVariable.class, MyIntArrayVariable.class, "array_int_maximum",
                constraintFactory);
    }

    public Constraint array_int_minimum(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.array_int_minimum(ConstrainedIntVariable.class, MyIntArrayVariable.class, "array_int_maximum",
                constraintFactory);
    }

    public Constraint array_var_int_element(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.array_var_int_element(MyIntVariable.class, MyIntArrayVariable.class,
                ConstrainedIntVariable.class, "array_var_int_element", constraintFactory);
    }

    public Constraint int_abs(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_abs(MyIntVariable.class, ConstrainedIntVariable.class, "int_abs", constraintFactory);
    }

    public Constraint int_div(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_div(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class, "int_div",
                constraintFactory);
    }

    public Constraint int_eq(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_eq(MyIntVariable.class, ConstrainedIntVariable.class, "int_eq", constraintFactory);
    }

    public Constraint int_eq_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_eq_reif(MyIntVariable.class, ConstrainedIntVariable.class, MySecondIntVariable.class,
                "int_eq_reif", constraintFactory);
    }

    public Constraint int_le(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_le(MyIntVariable.class, ConstrainedIntVariable.class, "int_le", constraintFactory);
    }

    public Constraint int_le_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_le_reif(MyIntVariable.class, ConstrainedIntVariable.class, MySecondIntVariable.class,
                "int_le_reif", constraintFactory);
    }

    public Constraint int_lin_eq(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_eq(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, "int_lin_eq", constraintFactory);
    }

    public Constraint int_lin_eq_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_eq_reif(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, MyIntVariable.class,
                "int_lin_eq_reif", constraintFactory);
    }

    public Constraint int_lin_le(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_le(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, "int_lin_le", constraintFactory);
    }

    public Constraint int_lin_le_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_le_reif(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, MyIntVariable.class,
                "int_lin_le_reif", constraintFactory);
    }

    public Constraint int_lin_ne(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_ne(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, "int_lin_ne", constraintFactory);
    }

    public Constraint int_lin_ne_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_ne_reif(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, MyIntVariable.class,
                "int_lin_ne_reif", constraintFactory);
    }

    public Constraint int_lt(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lt(MyIntVariable.class, ConstrainedIntVariable.class, "int_lt", constraintFactory);
    }

    public Constraint int_lt_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lt_reif(MyIntVariable.class, ConstrainedIntVariable.class, MySecondIntVariable.class,
                "int_lt_reif", constraintFactory);
    }

    public Constraint int_max(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_max(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class, "int_max",
                constraintFactory);
    }

    public Constraint int_min(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_min(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class, "int_min",
                constraintFactory);
    }

    public Constraint int_mod(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_mod(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class, "int_mod",
                constraintFactory);
    }

    public Constraint int_ne(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_ne(MyIntVariable.class, ConstrainedIntVariable.class, "int_ne", constraintFactory);
    }

    public Constraint int_ne_reif(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_ne_reif(MyIntVariable.class, ConstrainedIntVariable.class, MySecondIntVariable.class,
                "int_ne_reif", constraintFactory);
    }

    public Constraint int_plus(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_plus(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class,
                "int_plus", constraintFactory);
    }

    public Constraint int_pow(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_pow(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class, "int_pow",
                constraintFactory);
    }

    public Constraint int_times(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_times(MyIntVariable.class, MySecondIntVariable.class, ConstrainedIntVariable.class,
                "int_times", constraintFactory);
    }

    public Constraint set_in(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.set_in(MyIntVariable.class, new int[] { 1, 2, 3, 5 }, "set_in", constraintFactory);
    }
}
