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
import static org.drools.model.DSL.exists;
import static org.drools.model.DSL.not;
import static org.drools.model.PatternDSL.betaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.drools.model.BetaIndex4;
import org.drools.model.DSL;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.functions.Function4;
import org.drools.model.functions.Predicate5;
import org.drools.model.functions.accumulate.AccumulateFunction;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.DroolsVariableFactory;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.FilteringPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.NonePentaJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

/**
 * Represents the left hand side of a Drools rule, the result of which are four variables.
 * For more, see {@link UniLeftHandSide} and {@link BiLeftHandSide}.
 *
 * @param <A> generic type of the first resulting variable
 * @param <B> generic type of the second resulting variable
 * @param <C> generic type of the third resulting variable
 * @param <D> generic type of the fourth resulting variable
 */
public final class QuadLeftHandSide<A, B, C, D> extends AbstractLeftHandSide {

    private final PatternVariable<A, ?, ?> patternVariableA;
    private final PatternVariable<B, ?, ?> patternVariableB;
    private final PatternVariable<C, ?, ?> patternVariableC;
    private final PatternVariable<D, ?, ?> patternVariableD;
    private final QuadRuleContext<A, B, C, D> ruleContext;

    protected QuadLeftHandSide(Variable<A> variableA, Variable<B> variableB, Variable<C> variableC,
            PatternVariable<D, ?, ?> patternVariableD, DroolsVariableFactory variableFactory) {
        this(new DetachedPatternVariable<>(variableA), new DetachedPatternVariable<>(variableB),
                new DetachedPatternVariable<>(variableC), patternVariableD, variableFactory);
    }

    protected QuadLeftHandSide(PatternVariable<A, ?, ?> patternVariableA, PatternVariable<B, ?, ?> patternVariableB,
            PatternVariable<C, ?, ?> patternVariableC, PatternVariable<D, ?, ?> patternVariableD,
            DroolsVariableFactory variableFactory) {
        super(variableFactory);
        this.patternVariableA = Objects.requireNonNull(patternVariableA);
        this.patternVariableB = Objects.requireNonNull(patternVariableB);
        this.patternVariableC = Objects.requireNonNull(patternVariableC);
        this.patternVariableD = Objects.requireNonNull(patternVariableD);
        this.ruleContext = buildRuleContext();
    }

    private QuadRuleContext<A, B, C, D> buildRuleContext() {
        ViewItem<?>[] viewItems = Stream.of(patternVariableA, patternVariableB, patternVariableC, patternVariableD)
                .flatMap(variable -> variable.build().stream())
                .toArray(size -> new ViewItem<?>[size]);
        return new QuadRuleContext<>(patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                patternVariableC.getPrimaryVariable(), patternVariableD.getPrimaryVariable(), viewItems);
    }

    public QuadLeftHandSide<A, B, C, D> andFilter(QuadPredicate<A, B, C, D> predicate) {
        return new QuadLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD.filter(predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable()),
                variableFactory);
    }

    private <E> QuadLeftHandSide<A, B, C, D> applyJoiners(Class<E> otherFactType,
            AbstractPentaJoiner<A, B, C, D, E> joiner, PentaPredicate<A, B, C, D, E> predicate, boolean shouldExist) {
        Variable<E> toExist = (Variable<E>) variableFactory.createVariable(otherFactType, "toExist");
        PatternDSL.PatternDef<E> existencePattern = pattern(toExist);
        if (joiner == null) {
            return applyFilters(existencePattern, predicate, shouldExist);
        }
        JoinerType[] joinerTypes = joiner.getJoinerTypes();
        for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
            JoinerType joinerType = joinerTypes[mappingIndex];
            QuadFunction<A, B, C, D, Object> leftMapping = joiner.getLeftMapping(mappingIndex);
            Function<E, Object> rightMapping = joiner.getRightMapping(mappingIndex);
            Predicate5<E, A, B, C, D> joinPredicate =
                    (e, a, b, c, d) -> joinerType.matches(leftMapping.apply(a, b, c, d), rightMapping.apply(e));
            BetaIndex4<E, A, B, C, D, ?> index = betaIndexedBy(Object.class, getConstraintType(joinerType), mappingIndex,
                    rightMapping::apply, leftMapping::apply, Object.class);
            existencePattern = existencePattern.expr("Join using joiner #" + mappingIndex + " in " + joiner,
                    patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                    patternVariableC.getPrimaryVariable(), patternVariableD.getPrimaryVariable(), joinPredicate, index);
        }
        return applyFilters(existencePattern, predicate, shouldExist);
    }

    private <E> QuadLeftHandSide<A, B, C, D> applyFilters(PatternDSL.PatternDef<E> existencePattern,
            PentaPredicate<A, B, C, D, E> predicate, boolean shouldExist) {
        PatternDSL.PatternDef<E> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable(),
                        patternVariableD.getPrimaryVariable(), (e, a, b, c, d) -> predicate.test(a, b, c, d, e));
        ViewItem<?> existenceExpression = exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = not(possiblyFilteredExistencePattern);
        }
        return new QuadLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD.addDependentExpression(existenceExpression), variableFactory);
    }

    private <E> QuadLeftHandSide<A, B, C, D> existsOrNot(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners,
            boolean shouldExist) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        AbstractPentaJoiner<A, B, C, D, E> finalJoiner = null;
        PentaPredicate<A, B, C, D, E> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractPentaJoiner<A, B, C, D, E> joiner = (AbstractPentaJoiner<A, B, C, D, E>) joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NonePentaJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringPentaJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractPentaJoiner.merge(finalJoiner, joiner);
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

    public <E> QuadLeftHandSide<A, B, C, D> andExists(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners) {
        return existsOrNot(dClass, joiners, true);
    }

    public <E> QuadLeftHandSide<A, B, C, D> andNotExists(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners) {
        return existsOrNot(dClass, joiners, false);
    }

    public <NewA> UniLeftHandSide<NewA> andGroupBy(QuadConstraintCollector<A, B, C, D, ?, NewA> collector) {
        Variable<NewA> accumulateOutput = variableFactory.createVariable("collected");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collector, accumulateOutput));
        return new UniLeftHandSide<>(accumulateOutput, singletonList(outerAccumulatePattern), variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB) {
        Variable<NewA> accumulateOutputA = variableFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = variableFactory.createVariable("collectedB");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB));
        return new BiLeftHandSide<>(accumulateOutputA,
                new DirectPatternVariable<>(accumulateOutputB, outerAccumulatePattern), variableFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<NewA> accumulateOutputA = variableFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = variableFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = variableFactory.createVariable("collectedC");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(accumulateOutputA, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, outerAccumulatePattern),
                variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<NewA> accumulateOutputA = variableFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = variableFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = variableFactory.createVariable("collectedC");
        Variable<NewD> accumulateOutputD = variableFactory.createVariable("collectedD");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(accumulateOutputA, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, outerAccumulatePattern), variableFactory);
    }

    /**
     * Creates a Drools accumulate function based on a given collector. The accumulate function will take one
     * {@link Variable} as input and return its result into another {@link Variable}.
     *
     * @param <Out> type of the accumulate result
     * @param collector collector to use in the accumulate function
     * @param out variable in which to store accumulate result
     * @return Drools accumulate function
     */
    private <Out> AccumulateFunction createAccumulateFunction(QuadConstraintCollector<A, B, C, D, ?, Out> collector,
            Variable<Out> out) {
        Variable<A> variableA = patternVariableA.getPrimaryVariable();
        Variable<B> variableB = patternVariableB.getPrimaryVariable();
        Variable<C> variableC = patternVariableC.getPrimaryVariable();
        Variable<D> variableD = patternVariableD.getPrimaryVariable();
        return new AccumulateFunction(null,
                () -> new QuadAccumulator<>(variableA, variableB, variableC, variableD, collector))
                        .with(variableA, variableB, variableC, variableD)
                        .as(out);
    }

    public <NewA> UniLeftHandSide<NewA> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMapping) {
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMapping::apply);
        return new UniLeftHandSide<>(groupKey, singletonList(groupByPattern), variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMappingA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB) {
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutput = variableFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA::apply,
                createAccumulateFunction(collectorB, accumulateOutput));
        return new BiLeftHandSide<>(groupKey, new DirectPatternVariable<>(accumulateOutput, groupByPattern),
                variableFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMappingA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = variableFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = variableFactory.createVariable("outputC");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA::apply,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(groupKey, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, groupByPattern), variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<NewA> groupKey = variableFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = variableFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = variableFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = variableFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA::apply,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(groupKey, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, groupByPattern), variableFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMappingA,
            QuadFunction<A, B, C, D, NewB> keyMappingB) {
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, createCompositeBiGroupKey(keyMappingA, keyMappingB));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        DirectPatternVariable<BiTuple<NewA, NewB>> tuplePatternVar = decompose(groupKey, groupByPattern, newA, newB);
        PatternVariable<NewB, BiTuple<NewA, NewB>, ?> bPatternVar =
                new IndirectPatternVariable<>(tuplePatternVar, newB, tuple -> tuple.b);
        return new BiLeftHandSide<>(newA, bPatternVar, variableFactory);
    }

    /**
     * Takes group key mappings and merges them in such a way that the result is a single composite key.
     * This is necessary because Drools groupBy can only take a single key - therefore multiple variables need to be
     * converted into a singular composite variable.
     *
     * @param keyMappingA mapping for the first variable
     * @param keyMappingB mapping for the second variable
     * @param <NewA> generic type of the first variable
     * @param <NewB> generic type of the second variable
     * @return never null, Drools function to convert the keys to a singular composite key
     */
    private <NewA, NewB> Function4<A, B, C, D, BiTuple<NewA, NewB>> createCompositeBiGroupKey(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB) {
        return (a, b, c, d) -> new BiTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMappingA,
            QuadFunction<A, B, C, D, NewB> keyMappingB, QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutput = variableFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeBiGroupKey(keyMappingA, keyMappingB),
                createAccumulateFunction(collectorC, accumulateOutput));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        DirectPatternVariable<BiTuple<NewA, NewB>> tuplePatternVar = decompose(groupKey, groupByPattern, newA, newB);
        return new TriLeftHandSide<>(newA, newB, new DirectPatternVariable<>(accumulateOutput, tuplePatternVar.build()),
                variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<BiTuple<NewA, NewB>> groupKey =
                (Variable<BiTuple<NewA, NewB>>) variableFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutputC = variableFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = variableFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeBiGroupKey(keyMappingA, keyMappingB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        DirectPatternVariable<BiTuple<NewA, NewB>> tuplePatternVar = decompose(groupKey, groupByPattern, newA, newB);
        return new QuadLeftHandSide<>(newA, newB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, tuplePatternVar.build()), variableFactory);
    }

    /**
     * Takes group key mappings and merges them in such a way that the result is a single composite key.
     * This is necessary because Drools groupBy can only take a single key - therefore multiple variables need to be
     * converted into a singular composite variable.
     *
     * @param keyMappingA mapping for the first variable
     * @param keyMappingB mapping for the second variable
     * @param keyMappingC mapping for the third variable
     * @param <NewA> generic type of the first variable
     * @param <NewB> generic type of the second variable
     * @param <NewC> generic type of the third variable
     * @return never null, Drools function to convert the keys to a singular composite key
     */
    private <NewA, NewB, NewC> Function4<A, B, C, D, TriTuple<NewA, NewB, NewC>> createCompositeTriGroupKey(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB,
            QuadFunction<A, B, C, D, NewC> keyMappingC) {
        return (a, b, c, d) -> new TriTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d),
                keyMappingC.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(QuadFunction<A, B, C, D, NewA> keyMappingA,
            QuadFunction<A, B, C, D, NewB> keyMappingB, QuadFunction<A, B, C, D, NewC> keyMappingC) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey =
                (Variable<TriTuple<NewA, NewB, NewC>>) variableFactory.createVariable(TriTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeTriGroupKey(keyMappingA, keyMappingB, keyMappingC));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        Variable<NewC> newC = variableFactory.createVariable("newC");
        DirectPatternVariable<TriTuple<NewA, NewB, NewC>> tuplePatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC);
        PatternVariable<NewC, TriTuple<NewA, NewB, NewC>, ?> cPatternVar =
                new IndirectPatternVariable<>(tuplePatternVar, newC, tuple -> tuple.c);
        return new TriLeftHandSide<>(newA, newB, cPatternVar, variableFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB,
            QuadFunction<A, B, C, D, NewC> keyMappingC, QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey =
                (Variable<TriTuple<NewA, NewB, NewC>>) variableFactory.createVariable(TriTuple.class, "groupKey");
        Variable<NewD> accumulateOutputD = variableFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeTriGroupKey(keyMappingA, keyMappingB, keyMappingC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        Variable<NewC> newC = variableFactory.createVariable("newC");
        DirectPatternVariable<TriTuple<NewA, NewB, NewC>> tuplePatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC);
        return new QuadLeftHandSide<>(newA, newB, newC,
                new DirectPatternVariable<>(accumulateOutputD, tuplePatternVar.build()), variableFactory);
    }

    /**
     * Takes group key mappings and merges them in such a way that the result is a single composite key.
     * This is necessary because Drools groupBy can only take a single key - therefore multiple variables need to be
     * converted into a singular composite variable.
     *
     * @param keyMappingA mapping for the first variable
     * @param keyMappingB mapping for the second variable
     * @param keyMappingC mapping for the third variable
     * @param <NewA> generic type of the first variable
     * @param <NewB> generic type of the second variable
     * @param <NewC> generic type of the third variable
     * @return never null, Drools function to convert the keys to a singular composite key
     */
    private <NewA, NewB, NewC, NewD> Function4<A, B, C, D, QuadTuple<NewA, NewB, NewC, NewD>>
            createCompositeQuadGroupKey(QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB,
                    QuadFunction<A, B, C, D, NewC> keyMappingC, QuadFunction<A, B, C, D, NewD> keyMappingD) {
        return (a, b, c, d) -> new QuadTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d),
                keyMappingC.apply(a, b, c, d), keyMappingD.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadFunction<A, B, C, D, NewA> keyMappingA, QuadFunction<A, B, C, D, NewB> keyMappingB,
            QuadFunction<A, B, C, D, NewC> keyMappingC, QuadFunction<A, B, C, D, NewD> keyMappingD) {
        Variable<QuadTuple<NewA, NewB, NewC, NewD>> groupKey =
                (Variable<QuadTuple<NewA, NewB, NewC, NewD>>) variableFactory.createVariable(QuadTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeQuadGroupKey(keyMappingA, keyMappingB, keyMappingC, keyMappingD));
        Variable<NewA> newA = variableFactory.createVariable("newA");
        Variable<NewB> newB = variableFactory.createVariable("newB");
        Variable<NewC> newC = variableFactory.createVariable("newC");
        Variable<NewD> newD = variableFactory.createVariable("newD");
        DirectPatternVariable<QuadTuple<NewA, NewB, NewC, NewD>> tuplePatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC, newD);
        PatternVariable<NewD, QuadTuple<NewA, NewB, NewC, NewD>, ?> dPatternVar =
                new IndirectPatternVariable<>(tuplePatternVar, newD, tuple -> tuple.d);
        return new QuadLeftHandSide<>(newA, newB, newC, dPatternVar, variableFactory);
    }

    public <NewA> UniLeftHandSide<NewA> andMap(QuadFunction<A, B, C, D, NewA> mapping) {
        Variable<NewA> newA = variableFactory.createVariable("mapped", patternVariableA.getPrimaryVariable(),
                patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable(),
                patternVariableD.getPrimaryVariable(), mapping);
        List<ViewItem<?>> allPrerequisites = mergeViewItems(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD);
        DirectPatternVariable<NewA> newPatternVariableA = new DirectPatternVariable<>(newA, allPrerequisites);
        return new UniLeftHandSide<>(newPatternVariableA, variableFactory);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate() {
        return ruleContext.newRuleBuilder();
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToIntQuadFunction<A, B, C, D> matchWeighter) {
        return ruleContext.newRuleBuilder(matchWeighter);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToLongQuadFunction<A, B, C, D> matchWeighter) {
        return ruleContext.newRuleBuilder(matchWeighter);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        return ruleContext.newRuleBuilder(matchWeighter);
    }

    private ViewItem<?> buildAccumulate(AccumulateFunction... accFunctions) {
        ViewItem<?> innerAccumulatePattern =
                joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, patternVariableC, patternVariableD);
        return buildAccumulate(innerAccumulatePattern, accFunctions);
    }

    private <GroupKey_> ViewItem<?> buildGroupBy(Variable<GroupKey_> groupKey,
            Function4<A, B, C, D, GroupKey_> groupKeyExtractor, AccumulateFunction... accFunctions) {
        Variable<A> inputA = patternVariableA.getPrimaryVariable();
        Variable<B> inputB = patternVariableB.getPrimaryVariable();
        Variable<C> inputC = patternVariableC.getPrimaryVariable();
        Variable<D> inputD = patternVariableD.getPrimaryVariable();
        ViewItem<?> innerGroupByPattern =
                joinViewItemsWithLogicalAnd(patternVariableA, patternVariableB, patternVariableC, patternVariableD);
        return DSL.groupBy(innerGroupByPattern, inputA, inputB, inputC, inputD, groupKey, groupKeyExtractor,
                accFunctions);
    }

}
