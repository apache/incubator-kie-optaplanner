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

import java.util.function.Function;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.Joiners;

/**
 * Contains builtin predicates for the FlatZinc language.
 * Note: is_defined_var / defines_var pair is implemented
 * using CustomShadowVariable (See https://www.minizinc.org/doc-2.5.5/en/lib-stdlib.html#index-13 /
 * https://www.minizinc.org/doc-2.5.5/en/lib-stdlib.html#index-23).
 * See https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html
 */
public class FlatZincBuiltIns {
    // Private default constructor since this is a factory class
    private FlatZincBuiltIns() {
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-0
    /**
     * Constrains array [ indexVariable ] = constrainedVariable
     *
     * @param indexVariableClass The class of the variable index into the constant array
     * @param array The constant array of ints
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint array_int_element(Class<? extends IntVariable> indexVariableClass, int[] array,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(indexVariableClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal(indexVariable -> array[indexVariable.getValue()], IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-1
    /**
     * Constrains constrainedVariable to be the maximum value of the (non-empty) array arrayVariable
     *
     * @param constrainedVariableClass The class of the variable being constrained
     * @param arrayVariableClass The class of the variable array
     *
     * @return never null
     */
    public static Constraint array_int_maximum(Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntArrayVariable> arrayVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayVariableClass)
                .groupBy(ConstraintCollectors.max(IntArrayVariable::getValue))
                .ifNotExists(constrainedVariableClass, Joiners.equal(Function.identity(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-2
    /**
     * Constrains constrainedVariable to be the minimum value of the (non-empty) array arrayVariable
     *
     * @param constrainedVariableClass The class of the variable being constrained
     * @param arrayVariableClass The class of the variable array
     *
     * @return never null
     */
    public static Constraint array_int_minimum(Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntArrayVariable> arrayVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayVariableClass)
                .groupBy(ConstraintCollectors.min(IntArrayVariable::getValue))
                .ifNotExists(constrainedVariableClass, Joiners.equal(Function.identity(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-3
    /**
     * Constrains arrayVariable [ indexVariable ] = constrainedVariable
     *
     * @param indexVariableClass The class of the variable index into the constant array
     * @param arrayVariableClass The class of the variable array of ints
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint array_var_int_element(Class<? extends IntVariable> indexVariableClass,
            Class<? extends IntArrayVariable> arrayVariableClass, Class<? extends IntVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayVariableClass)
                .join(indexVariableClass,
                        Joiners.filtering((array, index) -> array.getIndex(arrayVariableClass).hasIndex(index.getValue())))
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((arrayElement, index) -> arrayElement.getValue(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-3
    /**
     * Constrains a variable to be the absolute value of another variable
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint int_abs(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal(value -> Math.abs(value.getValue()), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    public static int getEffectiveMultiplier(int[] multipliers, IndexSet multiplierIndexSet) {
        return multiplierIndexSet.indexBitSet.stream().map(index -> multipliers[index]).reduce(Integer::sum).orElse(0);
    }

    // Constrains c≠∑as[i]∗bs[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-14
    public static Constraint int_lin_ne(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .filter(sum -> sum == constant)
                .penalize(id, HardSoftScore.ONE_HARD);
    }
}
