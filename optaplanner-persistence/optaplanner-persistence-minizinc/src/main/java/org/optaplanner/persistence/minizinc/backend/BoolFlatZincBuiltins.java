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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public class BoolFlatZincBuiltins {
    private BoolFlatZincBuiltins() {
    }

    // Constrains r↔⋀ias[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-27
    public static Constraint array_bool_and(Class<? extends BoolArrayVariable> arrayMarkerClass,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayMarkerClass)
                .groupBy(ConstraintCollectors.min(BoolVariable::getValue))
                .ifNotExists(constrainedValueClass, Joiners.equal(bool -> bool, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains as [ b ] = c
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-28
    public static Constraint array_bool_element(Class<? extends IntVariable> indexClass,
            boolean[] itemArray,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(indexClass)
                .ifNotExists(constrainedValueClass, Joiners.equal(index -> itemArray[index.getValue()],
                        BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r↔⋁ias[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-29
    public static Constraint array_bool_or(Class<? extends BoolArrayVariable> arrayMarkerClass,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayMarkerClass)
                .groupBy(ConstraintCollectors.max(BoolVariable::getValue))
                .ifNotExists(constrainedValueClass, Joiners.equal(bool -> bool, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    public static class XorConstraintCollector<T extends BoolArrayVariable>
            implements UniConstraintCollector<T, AtomicBoolean, Boolean> {

        public XorConstraintCollector() {
        }

        @Override
        public Supplier<AtomicBoolean> supplier() {
            // Typically, false is used as the base, but we
            // want the negation of the XOR, so we use true
            // as the base
            return () -> new AtomicBoolean(true);
        }

        @Override
        public BiFunction<AtomicBoolean, T, Runnable> accumulator() {
            return (currentValue, variable) -> {
                final boolean value = variable.getValue();
                currentValue.set(currentValue.get() ^ value);
                return () -> {
                    currentValue.set(currentValue.get() ^ value);
                };
            };
        }

        @Override
        public Function<AtomicBoolean, Boolean> finisher() {
            return AtomicBoolean::get;
        }
    }

    // Constrains ⊕i as[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-30
    public static Constraint array_bool_xor(Class<? extends BoolArrayVariable> arrayMarkerClass,
            String id,
            ConstraintFactory constraintFactory) {
        return constraintFactory.from(arrayMarkerClass)
                .groupBy(new XorConstraintCollector<>())
                .filter(Boolean::booleanValue)
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains as [ b ] = c
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-31
    public static Constraint array_var_bool_element(Class<? extends IntVariable> indexClass,
            Class<? extends BoolArrayVariable> arrayMarkerClass,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(indexClass)
                .join(arrayMarkerClass)
                .filter((index, arrayElement) -> arrayElement.getIndex(arrayMarkerClass).hasIndex(index.getValue()))
                .ifNotExists(constrainedValueClass, Joiners.equal((index, arrayElement) -> arrayElement.getValue(),
                        BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains b∈{0,1} and a↔b=1
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-32
    public static Constraint bool2int(Class<? extends BoolVariable> booleanClass,
            Class<? extends IntVariable> integerClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(booleanClass)
                .join(integerClass)
                .filter((booleanVariable, intVariable) -> {
                    final int value = intVariable.getValue();
                    if (value > 1 || value < 0) {
                        return true;
                    }
                    return (value == 1) != booleanVariable.getValue();
                })
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r↔a∧b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-33
    public static Constraint bool_and(Class<? extends BoolVariable> firstConjunctClass,
            Class<? extends BoolVariable> secondConjunctClass,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstConjunctClass)
                .join(secondConjunctClass)
                .ifNotExists(constrainedValueClass, Joiners.equal((first, second) -> first.getValue() && second.getValue(),
                        BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains ⋁ias[i]∨⋁j¬bs[j]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-34
    public static Constraint bool_clause(Class<? extends BoolArrayVariable> consequentArrayMarkerClass,
            Class<? extends BoolArrayVariable> predicateArrayMarkerClass,
            String id,
            ConstraintFactory constraintFactory) {
        return constraintFactory.from(consequentArrayMarkerClass)
                .groupBy(ConstraintCollectors.max(BoolArrayVariable::getValue))
                .join(constraintFactory.from(predicateArrayMarkerClass)
                        .groupBy(ConstraintCollectors.max(BoolArrayVariable::getValue)))
                .filter((consequent, predicate) -> predicate && !consequent)
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains a = b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-35
    public static Constraint bool_eq(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .ifNotExists(secondBoolVariableClass, Joiners.equal(BoolVariable::getValue, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r ↔ ( a = b )
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-36
    public static Constraint bool_eq_reif(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .join(secondBoolVariableClass)
                .ifNotExists(isReversedClass, Joiners.equal((a, b) -> a.getValue() == b.getValue(), BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains a <= b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-37
    public static Constraint bool_le(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .ifNotExists(secondBoolVariableClass, Joiners.lessThanOrEqual(BoolVariable::getValue, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r ↔ ( a <= b )
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-38
    public static Constraint bool_le_reif(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .join(secondBoolVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) <= 0, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains c=∑ias[i]∗bs[i]
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-39
    public static Constraint bool_lin_eq(int[] variableMultipliers, Class<? extends BoolArrayVariable> variableArrayClass,
            Class<? extends IntVariable> goalVariable, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.conditionally(BoolArrayVariable::getValue,
                        ConstraintCollectors.sum(variable -> FlatZincBuiltins.getEffectiveMultiplier(variableMultipliers,
                                variable.getIndex(variableArrayClass)))))
                .ifNotExists(goalVariable, Joiners.equal(sum -> sum, IntVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains ∑ias[i]∗bs[i]≤c
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-40
    public static Constraint bool_lin_le(int[] variableMultipliers, Class<? extends BoolArrayVariable> variableArrayClass,
            int constant, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(variableArrayClass)
                .filter(variable -> variable.getValue() != null)
                .groupBy(ConstraintCollectors.conditionally(BoolArrayVariable::getValue,
                        ConstraintCollectors.sum(variable -> FlatZincBuiltins.getEffectiveMultiplier(variableMultipliers,
                                variable.getIndex(variableArrayClass)))))
                .filter(sum -> sum > constant)
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains a < b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-41
    public static Constraint bool_lt(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .ifNotExists(secondBoolVariableClass, Joiners.lessThan(BoolVariable::getValue, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r ↔ ( a < b )
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-42
    public static Constraint bool_lt_reif(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .join(secondBoolVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) < 0, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains a != b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-43
    public static Constraint bool_not(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .ifExists(secondBoolVariableClass, Joiners.equal(BoolVariable::getValue, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r↔a∨b
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-44
    public static Constraint bool_or(Class<? extends BoolVariable> firstDisjunctClass,
            Class<? extends BoolVariable> secondDisjunctClass,
            Class<? extends BoolVariable> constrainedValueClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstDisjunctClass)
                .join(secondDisjunctClass)
                .ifNotExists(constrainedValueClass, Joiners.equal((first, second) -> first.getValue() || second.getValue(),
                        BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // Constrains r ↔ ( a = b )
    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-45
    public static Constraint bool_xor(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstBoolVariableClass)
                .join(secondBoolVariableClass)
                .ifExists(isReversedClass, Joiners.equal((a, b) -> a.getValue() == b.getValue(), BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    public static Constraint bool_xor(Class<? extends BoolVariable> firstBoolVariableClass,
            Class<? extends BoolVariable> secondBoolVariableClass,
            String id, ConstraintFactory constraintFactory) {
        // Why does this exist while bool_not (which is bool not equals) exists?
        return bool_not(firstBoolVariableClass, secondBoolVariableClass, id, constraintFactory);
    }

}
