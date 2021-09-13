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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
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

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-4
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

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-5
    /**
     * Constrains a / b = c
     *
     * @param dividendClass The class of the value to be divided
     * @param divisorClass The class of the value to be divided by
     * @param constrainedVariableClass The class of the variable being constrained to the quotient
     *
     * @return never null
     */
    public static Constraint int_div(Class<? extends IntVariable> dividendClass,
            Class<? extends IntVariable> divisorClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(dividendClass)
                .join(divisorClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> a.getValue() / b.getValue(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-6
    /**
     * Constrains a variable to be the equal to another variable
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint int_eq(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal(IntVariable::getValue, IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-7
    /**
     * Constrains (a = b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the equality is reversed
     *
     * @return never null
     */
    public static Constraint int_eq_reif(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().equals(b.getValue()), isReversed -> isReversed.getValue() == 1))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-8
    /**
     * Constrains a variable to be less than or equal to another variable
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint int_le(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.lessThanOrEqual(IntVariable::getValue, IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-7
    /**
     * Constrains (a <= b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the ordering is reversed
     *
     * @return never null
     */
    public static Constraint int_le_reif(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) <= 0,
                                isReversed -> isReversed.getValue() == 1))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    public static int getEffectiveMultiplier(int[] multipliers, IndexSet multiplierIndexSet) {
        return multiplierIndexSet.indexBitSet.stream().map(index -> multipliers[index]).reduce(Integer::sum).orElse(0);
    }

    // Constrains c=∑as[i]∗bs[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-10
    public static Constraint int_lin_eq(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .filter(sum -> sum != constant)
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains (c=∑as[i]∗bs[i]) if and only if r
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-11
    public static Constraint int_lin_eq_reif(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .ifNotExists(isReversedClass, Joiners.equal(sum -> sum == constant, r -> r.getValue().equals(1)))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains ∑as[i]∗bs[i]<=c
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-12
    public static Constraint int_lin_le(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .filter(sum -> sum > constant)
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains (∑as[i]∗bs[i] <= c) if and only if r
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-13
    public static Constraint int_lin_le_reif(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .ifNotExists(isReversedClass, Joiners.equal(sum -> sum > constant, r -> r.getValue().equals(1)))
                .penalize(id, HardSoftScore.ONE_HARD);
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

    // Constrains (c≠∑as[i]∗bs[i]) if and only if r
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-15
    public static Constraint int_lin_ne_reif(int[] variableMultipliers, Class<? extends IntArrayVariable> variableArrayClass,
            int constant, Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.sum(variable -> variable.getValue()
                        * getEffectiveMultiplier(variableMultipliers, variable.getIndex(variableArrayClass))))
                .ifNotExists(isReversedClass, Joiners.equal(sum -> sum != constant, r -> r.getValue().equals(1)))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-16
    /**
     * Constrains a variable to be less than another variable
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint int_lt(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.lessThan(IntVariable::getValue, IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-17
    /**
     * Constrains (a < b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the ordering is reversed
     *
     * @return never null
     */
    public static Constraint int_lt_reif(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) < 0,
                                isReversed -> isReversed.getValue() == 1))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-18
    /**
     * Constrains max(a,b) = c
     *
     * @param value1Class The class of the value to be divided
     * @param value2Class The class of the value to be divided by
     * @param constrainedVariableClass The class of the variable being constrained to the quotient
     *
     * @return never null
     */
    public static Constraint int_max(Class<? extends IntVariable> value1Class,
            Class<? extends IntVariable> value2Class,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(value1Class)
                .join(value2Class)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> Math.max(a.getValue(), b.getValue()), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-19
    /**
     * Constrains min(a,b) = c
     *
     * @param value1Class The class of the value to be divided
     * @param value2Class The class of the value to be divided by
     * @param constrainedVariableClass The class of the variable being constrained to the quotient
     *
     * @return never null
     */
    public static Constraint int_min(Class<? extends IntVariable> value1Class,
            Class<? extends IntVariable> value2Class,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(value1Class)
                .join(value2Class)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> Math.min(a.getValue(), b.getValue()), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-20
    /**
     * Constrains a % b = c
     *
     * @param dividendClass The class of the value to be divided
     * @param divisorClass The class of the value to be divided by
     * @param constrainedVariableClass The class of the variable being constrained to the remainder
     *
     * @return never null
     */
    public static Constraint int_mod(Class<? extends IntVariable> dividendClass,
            Class<? extends IntVariable> divisorClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(dividendClass)
                .join(divisorClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> a.getValue() % b.getValue(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-21
    /**
     * Constrains a variable to be not equal to another variable
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the variable being constrained
     *
     * @return never null
     */
    public static Constraint int_ne(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .ifExists(constrainedVariableClass,
                        Joiners.equal(IntVariable::getValue, IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-22
    /**
     * Constrains (a != b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the equality is reversed
     *
     * @return never null
     */
    public static Constraint int_ne_reif(Class<? extends IntVariable> valueClass,
            Class<? extends IntVariable> constrainedVariableClass,
            Class<? extends IntVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().equals(b.getValue()), isReversed -> isReversed.getValue() == 1))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-23
    /**
     * Constrains a + b = c
     *
     * @param firstTermClass The class of the value to be added
     * @param secondTermClass The class of the value to be added
     * @param constrainedVariableClass The class of the variable being constrained to the sum
     *
     * @return never null
     */
    public static Constraint int_plus(Class<? extends IntVariable> firstTermClass,
            Class<? extends IntVariable> secondTermClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstTermClass)
                .join(secondTermClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> a.getValue() + b.getValue(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-24
    /**
     * Constrains a to the power of b = c
     *
     * @param baseClass The class of the base
     * @param exponentClass The class of the exponent
     * @param constrainedVariableClass The class of the variable being constrained to the power
     *
     * @return never null
     */
    public static Constraint int_pow(Class<? extends IntVariable> baseClass,
            Class<? extends IntVariable> exponentClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(baseClass)
                .join(exponentClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> BigInteger.valueOf(a.getValue()).pow(b.getValue()).intValue(),
                                IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-25
    /**
     * Constrains a * b = c
     *
     * @param firstTermClass The class of the value to be multiplied
     * @param secondTermClass The class of the value to be multiplied by
     * @param constrainedVariableClass The class of the variable being constrained to the product
     *
     * @return never null
     */
    public static Constraint int_times(Class<? extends IntVariable> firstTermClass,
            Class<? extends IntVariable> secondTermClass,
            Class<? extends IntVariable> constrainedVariableClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstTermClass)
                .join(secondTermClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((a, b) -> a.getValue() * b.getValue(), IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains x ∈ S
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-26
    public static Constraint set_in(Class<? extends IntVariable> variableClass, int[] valueSetItems,
            String id, ConstraintFactory constraintFactory) {
        final Set<Integer> valueSet = new HashSet<>();
        for (int item : valueSetItems) {
            valueSet.add(item);
        }
        return constraintFactory.from(variableClass)
                .filter(variable -> !valueSet.contains(variable.getValue()))
                .penalize(id, HardSoftScore.ONE_HARD);
    }
}
