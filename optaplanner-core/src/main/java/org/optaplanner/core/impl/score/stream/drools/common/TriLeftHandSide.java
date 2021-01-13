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

package org.optaplanner.core.impl.score.stream.drools.common;

import static java.util.Collections.singletonList;
import static org.drools.model.DSL.*;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.*;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.DroolsVariableFactory;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.FilteringQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.NoneQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

public final class TriLeftHandSide<A, B, C> extends AbstractLeftHandSide {

    private final PatternVariable<A> patternVariableA;
    private final PatternVariable<B> patternVariableB;
    private final PatternVariable<C> patternVariableC;

    protected TriLeftHandSide(PatternVariable<A> patternVariableA, PatternVariable<B> patternVariableB,
            PatternVariable<C> patternVariableC, DroolsVariableFactory variableFactory) {
        super(variableFactory);
        this.patternVariableA = patternVariableA;
        this.patternVariableB = patternVariableB;
        this.patternVariableC = patternVariableC;
    }

    protected PatternVariable<A> getPatternVariableA() {
        return patternVariableA;
    }

    protected PatternVariable<B> getPatternVariableB() {
        return patternVariableB;
    }

    protected PatternVariable<C> getPatternVariableC() {
        return patternVariableC;
    }

    public TriLeftHandSide<A, B, C> filter(TriPredicate<A, B, C> filter) {
        return new TriLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC.filter(filter,
                patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable()), variableFactory);
    }

    private <D> TriLeftHandSide<A, B, C> applyJoiners(Class<D> otherFactType, AbstractQuadJoiner<A, B, C, D> joiner,
            QuadPredicate<A, B, C, D> predicate, boolean shouldExist) {
        if (joiner == null) {
            return applyFilters(otherFactType, predicate, shouldExist);
        }
        // There is no delta index in Drools, therefore we replace joining with a filter.
        QuadPredicate<A, B, C, D> joinFilter = joiner::matches;
        QuadPredicate<A, B, C, D> result = predicate == null ? joinFilter : joinFilter.and(predicate);
        // And finally we add the filter to the D pattern.
        return applyFilters(otherFactType, result, shouldExist);
    }

    private <D> TriLeftHandSide<A, B, C> applyFilters(Class<D> otherFactType, QuadPredicate<A, B, C, D> predicate,
            boolean shouldExist) {
        Variable<D> toExist = (Variable<D>) variableFactory.createVariable(otherFactType, "biToExist");
        PatternDSL.PatternDef<D> existencePattern = pattern(toExist);
        PatternDSL.PatternDef<D> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable(),
                        (d, a, b, c) -> predicate.test(a, b, c, d));
        ViewItem<?> existenceExpression = PatternDSL.exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = not(possiblyFilteredExistencePattern);
        }
        return new TriLeftHandSide<>(patternVariableA, patternVariableB,
                patternVariableC.addDependentExpression(existenceExpression), variableFactory);
    }

    private <D> TriLeftHandSide<A, B, C> existsOrNot(Class<D> dClass, QuadJoiner<A, B, C, D>[] joiners,
            boolean shouldExist) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        AbstractQuadJoiner<A, B, C, D> finalJoiner = null;
        QuadPredicate<A, B, C, D> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractQuadJoiner<A, B, C, D> joiner = (AbstractQuadJoiner<A, B, C, D>) joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NoneQuadJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringQuadJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractQuadJoiner.merge(finalJoiner, joiner);
                }
            } else {
                if (!hasAFilter) { // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // Merge all filters into one to avoid paying the penalty for lack of indexing more than once.
                finalFilter = finalFilter == null ? joiner.getFilter() : finalFilter.and(joiner.getFilter());
            }
        }
        return applyJoiners(dClass, finalJoiner, finalFilter, shouldExist);
    }

    public <D> TriLeftHandSide<A, B, C> exists(Class<D> dClass, QuadJoiner<A, B, C, D>[] joiners) {
        return existsOrNot(dClass, joiners, true);
    }

    public <D> TriLeftHandSide<A, B, C> notExists(Class<D> dClass, QuadJoiner<A, B, C, D>[] joiners) {
        return existsOrNot(dClass, joiners, false);
    }

    public <D> QuadLeftHandSide<A, B, C, D> join(UniLeftHandSide<D> right, QuadJoiner<A, B, C, D> joiner) {
        AbstractQuadJoiner<A, B, C, D> castJoiner = (AbstractQuadJoiner<A, B, C, D>) joiner;
        PatternVariable<D> filteredRight = right.getPatternVariableA()
                .filter(castJoiner::matches, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable());
        return new QuadLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC, filteredRight,
                variableFactory);
    }

    public <NewA> UniLeftHandSide<NewA> groupBy(TriFunction<A, B, C, NewA> keyMapping) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, patternVariableC);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, groupKey,
                keyMapping::apply);
        Variable<NewA> newA = (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey));
        return new UniLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)), variableFactory);
    }

    public <NewA> UniLeftHandSide<NewA> groupBy(TriConstraintCollector<A, B, C, ?, NewA> collector) {
        Variable<TriTuple<A, B, C>> accumulateSource =
                (Variable<TriTuple<A, B, C>>) variableFactory.createVariable(TriTuple.class, "source");
        PatternVariable<C> newPatternVariableC = patternVariableC.bind(accumulateSource,
                patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                (c, a, b) -> new TriTuple<>(a, b, c));
        Variable<NewA> outputVariable = variableFactory.createVariable("collected");
        ViewItem<?> innerAccumulatePattern =
                joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, newPatternVariableC);
        ViewItem<?> outerAccumulatePattern = PatternDSL.accumulate(innerAccumulatePattern,
                accFunction(() -> new DroolsTriAccumulateFunction<>(collector), accumulateSource).as(outputVariable));
        return new UniLeftHandSide<>(new PatternVariable<>(outputVariable, singletonList(outerAccumulatePattern)),
                variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> groupBy(TriFunction<A, B, C, NewA> keyMappingA,
            TriFunction<A, B, C, NewB> keyMappingB) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, patternVariableC);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, groupKey,
                (a, b, c) -> new BiTuple<>(keyMappingA.apply(a, b, c), keyMappingB.apply(a, b, c)));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        return new BiLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> groupBy(TriFunction<A, B, C, NewA> keyMappingA,
            TriConstraintCollector<A, B, C, ?, NewB> collectorB) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<TriTuple<A, B, C>> accumulateSource =
                (Variable<TriTuple<A, B, C>>) variableFactory.createVariable(TriTuple.class, "source");
        PatternVariable<C> newPatternVariableC = patternVariableC.bind(accumulateSource,
                patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                (c, a, b) -> new TriTuple<>(a, b, c));
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        Variable<NewB> output = variableFactory.createVariable("output");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, newPatternVariableC);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, groupKey,
                keyMappingA::apply,
                accFunction(() -> new DroolsTriAccumulateFunction<>(collectorB), accumulateSource).as(output));
        Variable<NewA> newA = (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey));
        Variable<NewB> newB = (Variable<NewB>) variableFactory.createVariable("newB", from(output));
        return new BiLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), variableFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> groupBy(TriFunction<A, B, C, NewA> keyMappingA,
            TriFunction<A, B, C, NewB> keyMappingB, TriConstraintCollector<A, B, C, ?, NewC> collectorC) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<TriTuple<A, B, C>> accumulateSource =
                (Variable<TriTuple<A, B, C>>) variableFactory.createVariable(TriTuple.class, "source");
        PatternVariable<C> newPatternVariableC = patternVariableC.bind(accumulateSource,
                patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                (c, a, b) -> new TriTuple<>(a, b, c));
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> output = variableFactory.createVariable("output");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, newPatternVariableC);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, groupKey,
                (a, b, c) -> new BiTuple<>(keyMappingA.apply(a, b, c), keyMappingB.apply(a, b, c)),
                accFunction(() -> new DroolsTriAccumulateFunction<>(collectorC), accumulateSource).as(output));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        Variable<NewC> newC = (Variable<NewC>) variableFactory.createVariable("newC", from(output));
        return new TriLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), new PatternVariable<>(newC), variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> groupBy(
            TriFunction<A, B, C, NewA> keyMappingA, TriFunction<A, B, C, NewB> keyMappingB,
            TriConstraintCollector<A, B, C, ?, NewC> collectorC, TriConstraintCollector<A, B, C, ?, NewD> collectorD) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<TriTuple<A, B, C>> accumulateSource =
                (Variable<TriTuple<A, B, C>>) variableFactory.createVariable(TriTuple.class, "source");
        PatternVariable<C> newPatternVariableC = patternVariableC.bind(accumulateSource,
                patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                (c, a, b) -> new TriTuple<>(a, b, c));
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> outputC = variableFactory.createVariable("outputC");
        Variable<NewD> outputD = variableFactory.createVariable("outputD");
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, newPatternVariableC);
        ViewItem<?> groupByPattern = PatternDSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, groupKey,
                (a, b, c) -> new BiTuple<>(keyMappingA.apply(a, b, c), keyMappingB.apply(a, b, c)),
                accFunction(() -> new DroolsTriAccumulateFunction<>(collectorC), accumulateSource).as(outputC),
                accFunction(() -> new DroolsTriAccumulateFunction<>(collectorD), accumulateSource).as(outputD));
        Variable<NewA> newA =
                (Variable<NewA>) variableFactory.createVariable("newA", from(groupKey, k -> k.a));
        Variable<NewB> newB =
                (Variable<NewB>) variableFactory.createVariable("newB", from(groupKey, k -> k.b));
        Variable<NewC> newC = (Variable<NewC>) variableFactory.createVariable("newC", from(outputC));
        Variable<NewD> newD = (Variable<NewD>) variableFactory.createVariable("newD", from(outputD));
        return new QuadLeftHandSide<>(new PatternVariable<>(newA, singletonList(groupByPattern)),
                new PatternVariable<>(newB), new PatternVariable<>(newC), new PatternVariable<>(newD), variableFactory);
    }

    public AbstractTriConstraintConsequence<A, B, C> impact() {
        return new TriConstraintDefaultConsequence<>(this);
    }

    public AbstractTriConstraintConsequence<A, B, C> impact(ToIntTriFunction<A, B, C> matchWeighter) {
        return new TriConstraintIntConsequence<>(this, matchWeighter);
    }

    public AbstractTriConstraintConsequence<A, B, C> impact(ToLongTriFunction<A, B, C> matchWeighter) {
        return new TriConstraintLongConsequence<>(this, matchWeighter);
    }

    public AbstractTriConstraintConsequence<A, B, C> impact(TriFunction<A, B, C, BigDecimal> matchWeighter) {
        return new TriConstraintBigDecimalConsequence<>(this, matchWeighter);
    }

    @Override
    public List<ViewItem<?>> get() {
        return Stream.of(patternVariableA, patternVariableB, patternVariableC)
                .flatMap(variable -> variable.build().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Variable[] getVariables() {
        return Stream.of(patternVariableA, patternVariableB, patternVariableC)
                .map(PatternVariable::getPrimaryVariable)
                .toArray(Variable[]::new);
    }
}
