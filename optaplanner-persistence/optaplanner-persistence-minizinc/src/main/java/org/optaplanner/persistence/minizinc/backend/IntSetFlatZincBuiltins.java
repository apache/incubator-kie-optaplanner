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

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.Joiners;

public class IntSetFlatZincBuiltins {
    // Private constructor; factory method
    private IntSetFlatZincBuiltins() {
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-47
    // Constrains intSetArray[indexVariable] = intSet
    public static Constraint array_set_element(Class<? extends IntVariable> indexVariableClass,
            int[][] intSetArray,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        IntSet[] constantIntSetArray = new IntSet[intSetArray.length];
        for (int i = 0; i < constantIntSetArray.length; i++) {
            constantIntSetArray[i] = new IntSet(intSetArray[i]);
        }

        return constraintFactory.from(indexVariableClass)
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal(indexVariable -> constantIntSetArray[indexVariable.getValue()],
                                IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-48
    // Constrains intSetArray[indexVariable] = intSet
    public static Constraint array_var_set_element(Class<? extends IntVariable> indexVariableClass,
            Class<? extends IntSetArrayVariable> arrayVariableClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(indexVariableClass)
                .join(arrayVariableClass, Joiners.filtering((index, intSet) -> intSet.getIndex(arrayVariableClass)
                        .hasIndex(index.getValue())))
                .ifNotExists(constrainedVariableClass,
                        Joiners.equal((index, indexSet) -> indexSet.getValue(),
                                IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-49
    public static Constraint set_card(Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends IntVariable> sizeVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(sizeVariableClass)
                .join(constrainedVariableClass,
                        Joiners.filtering(
                                (sizeVariable, intSetVariable) -> intSetVariable.getValue().size() != sizeVariable.getValue()))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-50
    public static Constraint set_diff(Class<? extends IntSetVariable> minuendVariableClass,
            Class<? extends IntSetVariable> subtrahendVariableClass,
            Class<? extends IntSetVariable> differenceVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(minuendVariableClass)
                .join(subtrahendVariableClass)
                .ifNotExists(differenceVariableClass, Joiners.equal((minuend, subtrahend) -> minuend.getValue()
                        .withoutIntSetElements(subtrahend.getValue()),
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-51
    public static Constraint set_eq(Class<? extends IntSetVariable> intSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(intSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.equal(IntSetVariable::getValue,
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-52
    /**
     * Constrains (a = b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the equality is reversed
     *
     * @return never null
     */
    public static Constraint set_eq_reif(Class<? extends IntSetVariable> valueClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().equals(b.getValue()), BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-53
    public static Constraint set_in(Class<? extends IntVariable> itemVariableClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(itemVariableClass)
                .join(constrainedVariableClass,
                        Joiners.filtering(
                                (itemVariable, intSetVariable) -> !intSetVariable.getValue().contains(itemVariable.getValue())))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-54
    public static Constraint set_in_reif(Class<? extends IntVariable> itemVariableClass,
            int[] intSetItems,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        IntSet intSet = new IntSet(intSetItems);
        return constraintFactory.from(itemVariableClass)
                .join(isReversedClass, Joiners.equal(item -> !intSet.contains(item.getValue()), BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-55
    public static Constraint set_in_reif(Class<? extends IntVariable> itemVariableClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends BoolVariable> isReversedClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(itemVariableClass)
                .join(constrainedVariableClass)
                .join(isReversedClass, Joiners.equal((item, intSet) -> !intSet.getValue().contains(item.getValue()),
                        BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-56
    public static Constraint set_intersect(Class<? extends IntSetVariable> firstSetClass,
            Class<? extends IntSetVariable> secondSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstSetClass)
                .join(secondSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.equal((firstSet, secondSet) -> firstSet.getValue()
                        .intersectingIntSetElements(secondSet.getValue()),
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-57
    public static Constraint set_le(Class<? extends IntSetVariable> intSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(intSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.lessThanOrEqual(IntSetVariable::getValue,
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-58
    public static Constraint set_le_reif(Class<? extends IntSetVariable> valueClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) <= 0, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-59
    public static Constraint set_lt(Class<? extends IntSetVariable> intSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(intSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.lessThan(IntSetVariable::getValue,
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-60
    public static Constraint set_lt_reif(Class<? extends IntSetVariable> valueClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().compareTo(b.getValue()) < 0, BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-61
    public static Constraint set_ne(Class<? extends IntSetVariable> intSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(intSetClass)
                .ifExists(constrainedVariableClass, Joiners.equal(IntSetVariable::getValue,
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-62
    /**
     * Constrains (a != b) if and only if r
     *
     * @param valueClass The class of the value
     * @param constrainedVariableClass The class of the constrained value
     * @param isReversedClass The class of the value that control if the equality is reversed
     *
     * @return never null
     */
    public static Constraint set_ne_reif(Class<? extends IntSetVariable> valueClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(valueClass)
                .join(constrainedVariableClass)
                .ifExists(isReversedClass,
                        Joiners.equal((a, b) -> a.getValue().equals(b.getValue()), BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-63
    public static Constraint set_subset(Class<? extends IntSetVariable> subsetClass,
            Class<? extends IntSetVariable> supersetClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(subsetClass)
                .ifNotExists(supersetClass,
                        Joiners.filtering((subset, superset) -> subset.getValue().isSubSetOf(superset.getValue())))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-64
    public static Constraint set_subset_reif(Class<? extends IntSetVariable> subsetClass,
            Class<? extends IntSetVariable> supersetClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(subsetClass)
                .join(supersetClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((subset, superset) -> subset.getValue().isSubSetOf(superset.getValue()),
                                BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-65
    public static Constraint set_superset(Class<? extends IntSetVariable> supersetClass,
            Class<? extends IntSetVariable> subsetClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(supersetClass)
                .ifNotExists(subsetClass,
                        Joiners.filtering((superset, subset) -> subset.getValue().isSubSetOf(superset.getValue())))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-66
    public static Constraint set_superset_reif(Class<? extends IntSetVariable> supersetClass,
            Class<? extends IntSetVariable> subsetClass,
            Class<? extends BoolVariable> isReversedClass, String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(supersetClass)
                .join(subsetClass)
                .ifNotExists(isReversedClass,
                        Joiners.equal((superset, subset) -> subset.getValue().isSubSetOf(superset.getValue()),
                                BoolVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-67
    public static Constraint set_symdiff(Class<? extends IntSetVariable> firstSetClass,
            Class<? extends IntSetVariable> secondSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstSetClass)
                .join(secondSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.equal((firstSet, secondSet) -> firstSet.getValue()
                        .symmetricDifference(secondSet.getValue()),
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }

    // https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html#index-68
    public static Constraint set_union(Class<? extends IntSetVariable> firstSetClass,
            Class<? extends IntSetVariable> secondSetClass,
            Class<? extends IntSetVariable> constrainedVariableClass,
            String id, ConstraintFactory constraintFactory) {
        return constraintFactory.from(firstSetClass)
                .join(secondSetClass)
                .ifNotExists(constrainedVariableClass, Joiners.equal((firstSet, secondSet) -> firstSet.getValue()
                        .withIntSetElements(secondSet.getValue()),
                        IntSetVariable::getValue))
                .penalize(id, HardSoftScore.ONE_HARD);
    }
}
