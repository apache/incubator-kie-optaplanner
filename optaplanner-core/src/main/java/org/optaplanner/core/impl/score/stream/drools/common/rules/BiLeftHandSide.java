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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import static java.util.Collections.singletonList;
import static org.drools.model.DSL.*;
import static org.drools.model.PatternDSL.pattern;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.score.stream.drools.bi.DroolsBiAccumulateFunction;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.FilteringTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

final class BiLeftHandSide<A, B> extends AbstractLeftHandSide {

    private final PatternVariable<A> patternVariableA;
    private final PatternVariable<B> patternVariableB;

    protected BiLeftHandSide(PatternVariable<A> left, PatternVariable<B> right, DroolsVariableFactory variableFactory) {
        super(variableFactory);
        this.patternVariableA = left;
        this.patternVariableB = right;
    }

    protected BiLeftHandSide(BiLeftHandSide<A, B> leftHandSide, PatternVariable<A> left, PatternVariable<B> right) {
        super(leftHandSide.variableFactory);
        this.patternVariableA = left;
        this.patternVariableB = right;
    }

    protected PatternVariable<A> getPatternVariableA() {
        return patternVariableA;
    }

    protected PatternVariable<B> getPatternVariableB() {
        return patternVariableB;
    }

    public BiLeftHandSide<A, B> filter(BiPredicate<A, B> filter) {
        return new BiLeftHandSide<>(this, patternVariableA,
                patternVariableB.filter(filter, patternVariableA.getPrimaryVariable()));
    }

    private <C> BiLeftHandSide<A, B> applyJoiners(Class<C> otherFactType, AbstractTriJoiner<A, B, C> joiner,
            TriPredicate<A, B, C> predicate, boolean shouldExist) {
        if (joiner == null) {
            return applyFilters(otherFactType, predicate, shouldExist);
        }
        // There is no gamma index in Drools, therefore we replace joining with a filter.
        TriPredicate<A, B, C> joinFilter = joiner::matches;
        TriPredicate<A, B, C> result = predicate == null ? joinFilter : joinFilter.and(predicate);
        // And finally we add the filter to the C pattern.
        return applyFilters(otherFactType, result, shouldExist);
    }

    private <C> BiLeftHandSide<A, B> applyFilters(Class<C> otherFactType, TriPredicate<A, B, C> predicate,
            boolean shouldExist) {
        Variable<C> toExist = (Variable<C>) variableFactory.createVariable(otherFactType, "biToExist");
        PatternDSL.PatternDef<C> existencePattern = pattern(toExist);
        PatternDSL.PatternDef<C> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), (c, a, b) -> predicate.test(a, b, c));
        ViewItem<?> existenceExpression = PatternDSL.exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = not(possiblyFilteredExistencePattern);
        }
        return new BiLeftHandSide<>(this, patternVariableA, patternVariableB.addDependentExpression(existenceExpression));
    }

    private <C> BiLeftHandSide<A, B> existsOrNot(Class<C> cClass, AbstractTriJoiner<A, B, C>[] joiners, boolean shouldExist) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        AbstractTriJoiner<A, B, C> finalJoiner = null;
        TriPredicate<A, B, C> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractTriJoiner<A, B, C> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NoneTriJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringTriJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractTriJoiner.merge(finalJoiner, joiner);
                }
            } else {
                if (!hasAFilter) { // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // Merge all filters into one to avoid paying the penalty for lack of indexing more than once.
                finalFilter = finalFilter == null ? joiner.getFilter() : finalFilter.and(joiner.getFilter());
            }
        }
        return applyJoiners(cClass, finalJoiner, finalFilter, shouldExist);
    }

    public <C> BiLeftHandSide<A, B> exists(Class<C> cClass, AbstractTriJoiner<A, B, C>[] joiners) {
        return existsOrNot(cClass, joiners, true);
    }

    public <C> BiLeftHandSide<A, B> notExists(Class<C> cClass, AbstractTriJoiner<A, B, C>[] joiners) {
        return existsOrNot(cClass, joiners, false);
    }

    public <C> TriLeftHandSide<A, B, C> join(UniLeftHandSide<C> right, AbstractTriJoiner<A, B, C> joiner) {
        PatternVariable<C> filteredRight = right.getPatternVariableA()
                .filter(joiner::matches, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable());
        return new TriLeftHandSide<>(patternVariableA, patternVariableB, filteredRight, variableFactory);
    }

    public <NewA> UniLeftHandSide<NewA> groupBy(BiFunction<A, B, NewA> keyMapping) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, groupKey,
                keyMapping::apply);
        Variable<NewA> newA = (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey));
        return new UniLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)), variableFactory);
    }

    public <NewA> UniLeftHandSide<NewA> groupBy(DroolsBiAccumulateFunction<A, B, ?, NewA> accFunction) {
        Variable<BiTuple<A, B>> accumulateSource =
                (Variable<BiTuple<A, B>>) variableFactory.createVariable(BiTuple.class, "source");
        PatternVariable<B> newPatternVariableB = patternVariableB.bind(accumulateSource,
                patternVariableA.getPrimaryVariable(), (b, a) -> new BiTuple<>(a, b));
        Variable<NewA> outputVariable = variableFactory.createVariable("collected");
        ViewItem<?> innerAccumulatePattern = joinViewItemsWithLogicalAnd(patternVariableA, newPatternVariableB);
        ViewItem<?> outerAccumulatePattern = PatternDSL.accumulate(innerAccumulatePattern,
                accFunction(() -> accFunction, accumulateSource).as(outputVariable));
        return new UniLeftHandSide<>(new PatternVariable<>(outputVariable, singletonList(outerAccumulatePattern)),
                variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> groupBy(BiFunction<A, B, NewA> keyMappingA,
            BiFunction<A, B, NewB> keyMappingB) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, groupKey,
                (a, b) -> new BiTuple<>(keyMappingA.apply(a, b), keyMappingB.apply(a, b)));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        return new BiLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> groupBy(BiFunction<A, B, NewA> keyMappingA,
            DroolsBiAccumulateFunction<A, B, ?, NewB> accFunctionB) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<BiTuple<A, B>> accumulateSource =
                (Variable<BiTuple<A, B>>) variableFactory.createVariable(BiTuple.class, "source");
        PatternVariable<B> newPatternVariableB = patternVariableB.bind(accumulateSource, inputA,
                (b, a) -> new BiTuple<>(a, b));
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        Variable<NewB> output = variableFactory.createVariable("output");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, newPatternVariableB);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, groupKey,
                keyMappingA::apply, accFunction(() -> accFunctionB, accumulateSource).as(output));
        Variable<NewA> newA = (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey));
        Variable<NewB> newB = (Variable<NewB>) variableFactory.createVariable("newB", from(output));
        return new BiLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), variableFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> groupBy(BiFunction<A, B, NewA> keyMappingA,
            BiFunction<A, B, NewB> keyMappingB, DroolsBiAccumulateFunction<A, B, ?, NewC> accFunctionC) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<BiTuple<A, B>> accumulateSource =
                (Variable<BiTuple<A, B>>) variableFactory.createVariable(BiTuple.class, "source");
        PatternVariable<B> newPatternVariableB = patternVariableB.bind(accumulateSource, inputA,
                (b, a) -> new BiTuple<>(a, b));
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> output = variableFactory.createVariable("output");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, newPatternVariableB);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, groupKey,
                (a, b) -> new BiTuple<>(keyMappingA.apply(a, b), keyMappingB.apply(a, b)),
                accFunction(() -> accFunctionC, accumulateSource).as(output));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        Variable<NewC> newC = (Variable<NewC>) variableFactory.createVariable("newC", from(output));
        return new TriLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), new PatternVariable<>(newC), variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> groupBy(BiFunction<A, B, NewA> keyMappingA,
            BiFunction<A, B, NewB> keyMappingB, DroolsBiAccumulateFunction<A, B, ?, NewC> accFunctionC,
            DroolsBiAccumulateFunction<A, B, ?, NewD> accFunctionD) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<BiTuple<A, B>> accumulateSource =
                (Variable<BiTuple<A, B>>) variableFactory.createVariable(BiTuple.class, "source");
        PatternVariable<B> newPatternVariableB = patternVariableB.bind(accumulateSource, inputA,
                (b, a) -> new BiTuple<>(a, b));
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> outputC = variableFactory.createVariable("outputC");
        Variable<NewD> outputD = variableFactory.createVariable("outputD");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, newPatternVariableB);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, groupKey,
                (a, b) -> new BiTuple<>(keyMappingA.apply(a, b), keyMappingB.apply(a, b)),
                accFunction(() -> accFunctionC, accumulateSource).as(outputC),
                accFunction(() -> accFunctionD, accumulateSource).as(outputD));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        Variable<NewC> newC = (Variable<NewC>) variableFactory.createVariable("newC", from(outputC));
        Variable<NewD> newD = (Variable<NewD>) variableFactory.createVariable("newD", from(outputD));
        return new QuadLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), new PatternVariable<>(newC), new PatternVariable<>(newD), variableFactory);
    }

    @Override
    public List<ViewItem<?>> get() {
        return Stream.of(patternVariableA, patternVariableB)
                .flatMap(variable -> variable.build().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Variable[] getVariables() {
        return Stream.of(patternVariableA, patternVariableB)
                .map(PatternVariable::getPrimaryVariable)
                .toArray(Variable[]::new);
    }
}
