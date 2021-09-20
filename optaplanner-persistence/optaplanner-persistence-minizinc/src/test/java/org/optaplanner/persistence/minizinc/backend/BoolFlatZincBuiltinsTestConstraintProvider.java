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

public class BoolFlatZincBuiltinsTestConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // intentionally empty; each constraint is tested individually
        };
    }

    public Constraint array_bool_and(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.array_bool_and(MyBoolArrayVariable.class, MyBoolVariable.class,
                "array_bool_and", constraintFactory);
    }

    public Constraint array_bool_element(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.array_bool_element(MyIntVariable.class, new boolean[] { false, true, false, true },
                MyBoolVariable.class, "array_bool_element", constraintFactory);
    }

    public Constraint array_bool_or(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.array_bool_or(MyBoolArrayVariable.class, MyBoolVariable.class,
                "array_bool_or", constraintFactory);
    }

    public Constraint array_bool_xor(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.array_bool_xor(MyBoolArrayVariable.class,
                "array_bool_xor", constraintFactory);
    }

    public Constraint array_var_bool_element(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.array_var_bool_element(MyIntVariable.class, MyBoolArrayVariable.class,
                MyBoolVariable.class, "array_var_bool_element", constraintFactory);
    }

    public Constraint bool2int(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool2int(MyBoolVariable.class, MyIntVariable.class,
                "bool2int", constraintFactory);
    }

    public Constraint bool_and(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_and(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_and", constraintFactory);
    }

    public Constraint bool_clause(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_clause(MyBoolArrayVariable.class, MySecondBoolArrayVariable.class,
                "bool_clause", constraintFactory);
    }

    public Constraint bool_eq(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_eq(MyBoolVariable.class, ConstrainedBoolVariable.class,
                "bool_eq", constraintFactory);
    }

    public Constraint bool_eq_reif(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_eq_reif(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_eq_reif", constraintFactory);
    }

    public Constraint bool_le(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_le(MyBoolVariable.class, ConstrainedBoolVariable.class,
                "bool_le", constraintFactory);
    }

    public Constraint bool_le_reif(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_le_reif(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_le_reif", constraintFactory);
    }

    public Constraint bool_lin_eq(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_lin_eq(new int[] { 1, 2, 3 }, MyBoolArrayVariable.class,
                MyIntVariable.class,
                "bool_lin_eq", constraintFactory);
    }

    public Constraint bool_lin_le(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_lin_le(new int[] { 1, 2, 3 }, MyBoolArrayVariable.class,
                3,
                "bool_lin_eq", constraintFactory);
    }

    public Constraint bool_lt(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_lt(MyBoolVariable.class, ConstrainedBoolVariable.class,
                "bool_lt", constraintFactory);
    }

    public Constraint bool_lt_reif(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_lt_reif(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_lt_reif", constraintFactory);
    }

    public Constraint bool_not(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_not(MyBoolVariable.class, ConstrainedBoolVariable.class,
                "bool_not", constraintFactory);
    }

    public Constraint bool_or(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_or(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_or", constraintFactory);
    }

    public Constraint bool_xor_3args(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_xor(MyBoolVariable.class, MySecondBoolVariable.class,
                ConstrainedBoolVariable.class,
                "bool_xor_3args", constraintFactory);
    }

    public Constraint bool_xor_2args(ConstraintFactory constraintFactory) {
        return BoolFlatZincBuiltins.bool_xor(MyBoolVariable.class, ConstrainedBoolVariable.class,
                "bool_xor_2args", constraintFactory);
    }
}
