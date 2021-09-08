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
                array_int_element(constraintFactory),
                array_int_maximum(constraintFactory),
                array_int_minimum(constraintFactory),
                array_var_int_element(constraintFactory),
                int_abs(constraintFactory),
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

    public Constraint int_lin_ne(ConstraintFactory constraintFactory) {
        return FlatZincBuiltIns.int_lin_ne(new int[] { 1, -1 }, MyIntArrayVariable.class, 0, "int_lin_ne", constraintFactory);
    }
}
