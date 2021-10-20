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

public class IntSetFlatZincBuiltinsTestConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // intentionally empty; each constraint is tested individually
        };
    }

    public Constraint array_set_element(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.array_set_element(MyIntVariable.class,
                new int[][] { { 1 }, { 2 }, { 1, 3 } },
                ConstrainedIntSetVariable.class,
                "array_set_element", constraintFactory);
    }

    public Constraint array_var_set_element(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.array_var_set_element(MyIntVariable.class,
                MyIntSetArrayVariable.class,
                ConstrainedIntSetVariable.class,
                "array_var_set_element", constraintFactory);
    }

    public Constraint set_card(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_card(ConstrainedIntSetVariable.class, MyIntVariable.class,
                "set_card", constraintFactory);
    }

    public Constraint set_diff(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_diff(MyIntSetVariable.class, MySecondIntSetVariable.class,
                ConstrainedIntSetVariable.class,
                "set_diff", constraintFactory);
    }

    public Constraint set_eq(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_eq(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_eq", constraintFactory);
    }

    public Constraint set_eq_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_eq_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class, MyBoolVariable.class,
                "set_eq_reif", constraintFactory);
    }

    public Constraint set_in(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_in(MyIntVariable.class, ConstrainedIntSetVariable.class, "set_in", constraintFactory);
    }

    public Constraint set_in_reif_const(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_in_reif(MyIntVariable.class, new int[] { 0, 1, 2 }, MyBoolVariable.class,
                "set_in_reif_const", constraintFactory);
    }

    public Constraint set_in_reif_var(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_in_reif(MyIntVariable.class, ConstrainedIntSetVariable.class, MyBoolVariable.class,
                "set_in_reif_var", constraintFactory);
    }

    public Constraint set_intersect(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_intersect(MyIntSetVariable.class, MySecondIntSetVariable.class,
                ConstrainedIntSetVariable.class,
                "set_intersect", constraintFactory);
    }

    public Constraint set_le(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_le(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_le", constraintFactory);
    }

    public Constraint set_le_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_le_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class, MyBoolVariable.class,
                "set_le_reif", constraintFactory);
    }

    public Constraint set_lt(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_lt(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_lt", constraintFactory);
    }

    public Constraint set_lt_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_lt_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class, MyBoolVariable.class,
                "set_lt_reif", constraintFactory);
    }

    public Constraint set_ne(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_ne(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_ne", constraintFactory);
    }

    public Constraint set_ne_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_ne_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class, MyBoolVariable.class,
                "set_ne_reif", constraintFactory);
    }

    public Constraint set_subset(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_subset(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_subset", constraintFactory);
    }

    public Constraint set_subset_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_subset_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                MyBoolVariable.class,
                "set_subset_reif", constraintFactory);
    }

    public Constraint set_superset(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_superset(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                "set_superset", constraintFactory);
    }

    public Constraint set_superset_reif(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_superset_reif(MyIntSetVariable.class, ConstrainedIntSetVariable.class,
                MyBoolVariable.class,
                "set_superset_reif", constraintFactory);
    }

    public Constraint set_symdiff(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_symdiff(MyIntSetVariable.class, MySecondIntSetVariable.class,
                ConstrainedIntSetVariable.class,
                "set_symdiff", constraintFactory);
    }

    public Constraint set_union(ConstraintFactory constraintFactory) {
        return IntSetFlatZincBuiltins.set_union(MyIntSetVariable.class, MySecondIntSetVariable.class,
                ConstrainedIntSetVariable.class,
                "set_union", constraintFactory);
    }
}
