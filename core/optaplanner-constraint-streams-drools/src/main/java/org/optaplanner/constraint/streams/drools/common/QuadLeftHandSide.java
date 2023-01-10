package org.optaplanner.constraint.streams.drools.common;

import static java.util.Collections.singletonList;
import static org.drools.model.DSL.exists;
import static org.drools.model.DSL.not;
import static org.drools.model.PatternDSL.betaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.drools.model.BetaIndex4;
import org.drools.model.DSL;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Function4;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.Predicate4;
import org.drools.model.functions.Predicate5;
import org.drools.model.functions.accumulate.AccumulateFunction;
import org.drools.model.view.ViewItem;
import org.optaplanner.constraint.streams.common.penta.DefaultPentaJoiner;
import org.optaplanner.constraint.streams.common.penta.FilteringPentaJoiner;
import org.optaplanner.constraint.streams.drools.DroolsInternalsFactory;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.impl.score.stream.JoinerType;

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

    QuadLeftHandSide(Variable<A> variableA, Variable<B> variableB, Variable<C> variableC,
            PatternVariable<D, ?, ?> patternVariableD, DroolsInternalsFactory internalsFactory) {
        this(new DetachedPatternVariable<>(variableA), new DetachedPatternVariable<>(variableB),
                new DetachedPatternVariable<>(variableC), patternVariableD, internalsFactory);
    }

    QuadLeftHandSide(PatternVariable<A, ?, ?> patternVariableA, PatternVariable<B, ?, ?> patternVariableB,
            PatternVariable<C, ?, ?> patternVariableC, PatternVariable<D, ?, ?> patternVariableD,
            DroolsInternalsFactory internalsFactory) {
        super(internalsFactory);
        this.patternVariableA = Objects.requireNonNull(patternVariableA);
        this.patternVariableB = Objects.requireNonNull(patternVariableB);
        this.patternVariableC = Objects.requireNonNull(patternVariableC);
        this.patternVariableD = Objects.requireNonNull(patternVariableD);
        this.ruleContext = buildRuleContext();
    }

    private QuadRuleContext<A, B, C, D> buildRuleContext() {
        ViewItem<?>[] viewItems = Stream.of(patternVariableA, patternVariableB, patternVariableC, patternVariableD)
                .flatMap(variable -> variable.build().stream())
                .toArray((IntFunction<ViewItem<?>[]>) ViewItem[]::new);
        return new QuadRuleContext<>(patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                patternVariableC.getPrimaryVariable(), patternVariableD.getPrimaryVariable(), viewItems);
    }

    public QuadLeftHandSide<A, B, C, D> andFilter(Predicate4<A, B, C, D> predicate) {
        return new QuadLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD.filter(predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable()),
                internalsFactory);
    }

    private <E> QuadLeftHandSide<A, B, C, D> applyJoiners(Class<E> otherFactType, Predicate1<E> nullityFilter,
            DefaultPentaJoiner<A, B, C, D, E> joiner, Predicate5<A, B, C, D, E> predicate, boolean shouldExist) {
        Variable<E> toExist = internalsFactory.createVariable(otherFactType, "toExist");
        PatternDSL.PatternDef<E> existencePattern = pattern(toExist);
        if (nullityFilter != null) {
            existencePattern = existencePattern.expr("Exclude nulls using " + nullityFilter, nullityFilter);
        }
        if (joiner == null) {
            return applyFilters(existencePattern, predicate, shouldExist);
        }
        int joinerCount = joiner.getJoinerCount();
        for (int mappingIndex = 0; mappingIndex < joinerCount; mappingIndex++) {
            JoinerType joinerType = joiner.getJoinerType(mappingIndex);
            Function4<A, B, C, D, Object> leftMapping = internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
            Function1<E, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
            Predicate5<E, A, B, C, D> joinPredicate =
                    (e, a, b, c, d) -> joinerType.matches(leftMapping.apply(a, b, c, d), rightMapping.apply(e));
            existencePattern = existencePattern.expr("Join using joiner #" + mappingIndex + " in " + joiner,
                    patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                    patternVariableC.getPrimaryVariable(), patternVariableD.getPrimaryVariable(), joinPredicate,
                    createBetaIndex(joiner, mappingIndex));
        }
        return applyFilters(existencePattern, predicate, shouldExist);
    }

    private <E> BetaIndex4<E, A, B, C, D, ?> createBetaIndex(DefaultPentaJoiner<A, B, C, D, E> joiner, int mappingIndex) {
        JoinerType joinerType = joiner.getJoinerType(mappingIndex);
        Function4<A, B, C, D, Object> leftMapping = internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
        Function1<E, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
        if (joinerType == JoinerType.EQUAL) {
            return betaIndexedBy(Object.class, getConstraintType(joinerType), mappingIndex, rightMapping, leftMapping,
                    Object.class);
        } else { // Drools beta index on LT/LTE/GT/GTE requires Comparable.
            JoinerType reversedJoinerType = joinerType.flip();
            // TODO fix the Comparable
            return betaIndexedBy(Comparable.class, getConstraintType(reversedJoinerType), mappingIndex,
                    e -> (Comparable) rightMapping.apply(e), leftMapping, Comparable.class);
        }
    }

    private <E> QuadLeftHandSide<A, B, C, D> applyFilters(PatternDSL.PatternDef<E> existencePattern,
            Predicate5<A, B, C, D, E> predicate, boolean shouldExist) {
        PatternDSL.PatternDef<E> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, patternVariableA.getPrimaryVariable(),
                        patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable(),
                        patternVariableD.getPrimaryVariable(), (e, a, b, c, d) -> predicate.test(a, b, c, d, e));
        ViewItem<?> existenceExpression = exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = not(possiblyFilteredExistencePattern);
        }
        return new QuadLeftHandSide<>(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD.addDependentExpression(existenceExpression), internalsFactory);
    }

    private <E> QuadLeftHandSide<A, B, C, D> existsOrNot(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners,
            Predicate1<E> nullityFilter, boolean shouldExist) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        DefaultPentaJoiner<A, B, C, D, E> finalJoiner = null;
        Predicate5<A, B, C, D, E> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            PentaJoiner<A, B, C, D, E> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof FilteringPentaJoiner) {
                if (!hasAFilter) { // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // Merge all filters into one to avoid paying the penalty for lack of indexing more than once.
                FilteringPentaJoiner<A, B, C, D, E> castJoiner = (FilteringPentaJoiner<A, B, C, D, E>) joiner;
                Predicate5<A, B, C, D, E> convertedFilter = internalsFactory.convert(castJoiner.getFilter());
                finalFilter = finalFilter == null ? convertedFilter : internalsFactory.merge(finalFilter, convertedFilter);
            } else {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    DefaultPentaJoiner<A, B, C, D, E> castJoiner = (DefaultPentaJoiner<A, B, C, D, E>) joiner;
                    finalJoiner = finalJoiner == null ? castJoiner : finalJoiner.and(castJoiner);
                }
            }
        }
        return applyJoiners(dClass, nullityFilter, finalJoiner, finalFilter, shouldExist);
    }

    public <E> QuadLeftHandSide<A, B, C, D> andExists(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners,
            Predicate1<E> nullityFilter) {
        return existsOrNot(dClass, joiners, nullityFilter, true);
    }

    public <E> QuadLeftHandSide<A, B, C, D> andNotExists(Class<E> dClass, PentaJoiner<A, B, C, D, E>[] joiners,
            Predicate1<E> nullityFilter) {
        return existsOrNot(dClass, joiners, nullityFilter, false);
    }

    public <NewA> UniLeftHandSide<NewA> andGroupBy(QuadConstraintCollector<A, B, C, D, ?, NewA> collector) {
        Variable<NewA> accumulateOutput = internalsFactory.createVariable("collected");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collector, accumulateOutput));
        return new UniLeftHandSide<>(accumulateOutput, singletonList(outerAccumulatePattern), internalsFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB));
        return new BiLeftHandSide<>(accumulateOutputA,
                new DirectPatternVariable<>(accumulateOutputB, outerAccumulatePattern), internalsFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("collectedC");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(accumulateOutputA, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, outerAccumulatePattern),
                internalsFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("collectedC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("collectedD");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(accumulateOutputA, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, outerAccumulatePattern), internalsFactory);
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

    public <NewA> UniLeftHandSide<NewA> andGroupBy(Function4<A, B, C, D, NewA> keyMapping) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMapping);
        return new UniLeftHandSide<>(groupKey, singletonList(groupByPattern), internalsFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutput = internalsFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutput));
        return new BiLeftHandSide<>(groupKey, new DirectPatternVariable<>(accumulateOutput, groupByPattern),
                internalsFactory);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(groupKey, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, groupByPattern), internalsFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(groupKey, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, groupByPattern), internalsFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, createCompositeBiGroupKey(keyMappingA, keyMappingB));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        IndirectPatternVariable<NewB, BiTuple<NewA, NewB>> bPatternVar =
                decompose(groupKey, groupByPattern, newA, newB);
        return new BiLeftHandSide<>(newA, bPatternVar, internalsFactory);
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
    private <NewA, NewB> Function4<A, B, C, D, BiTuple<NewA, NewB>>
            createCompositeBiGroupKey(Function4<A, B, C, D, NewA> keyMappingA, Function4<A, B, C, D, NewB> keyMappingB) {
        return (a, b, c, d) -> new BiTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB, QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutput = internalsFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeBiGroupKey(keyMappingA, keyMappingB),
                createAccumulateFunction(collectorC, accumulateOutput));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        DirectPatternVariable<NewC> cPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, accumulateOutput);
        return new TriLeftHandSide<>(newA, newB, cPatternVar, internalsFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB, QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeBiGroupKey(keyMappingA, keyMappingB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        DirectPatternVariable<NewD> dPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, accumulateOutputD);
        return new QuadLeftHandSide<>(newA, newB, accumulateOutputC, dPatternVar, internalsFactory);
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
            Function4<A, B, C, D, NewA> keyMappingA, Function4<A, B, C, D, NewB> keyMappingB,
            Function4<A, B, C, D, NewC> keyMappingC) {
        return (a, b, c, d) -> new TriTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d),
                keyMappingC.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB, Function4<A, B, C, D, NewC> keyMappingC) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey = internalsFactory.createVariable(TriTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeTriGroupKey(keyMappingA, keyMappingB, keyMappingC));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        IndirectPatternVariable<NewC, TriTuple<NewA, NewB, NewC>> cPatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC);
        return new TriLeftHandSide<>(newA, newB, cPatternVar, internalsFactory);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB, Function4<A, B, C, D, NewC> keyMappingC,
            QuadConstraintCollector<A, B, C, D, ?, NewD> collectorD) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey = internalsFactory.createVariable(TriTuple.class, "groupKey");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeTriGroupKey(keyMappingA, keyMappingB, keyMappingC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        DirectPatternVariable<NewD> dPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, newC, accumulateOutputD);
        return new QuadLeftHandSide<>(newA, newB, newC, dPatternVar, internalsFactory);
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
            createCompositeQuadGroupKey(Function4<A, B, C, D, NewA> keyMappingA, Function4<A, B, C, D, NewB> keyMappingB,
                    Function4<A, B, C, D, NewC> keyMappingC, Function4<A, B, C, D, NewD> keyMappingD) {
        return (a, b, c, d) -> new QuadTuple<>(keyMappingA.apply(a, b, c, d), keyMappingB.apply(a, b, c, d),
                keyMappingC.apply(a, b, c, d), keyMappingD.apply(a, b, c, d));
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function4<A, B, C, D, NewA> keyMappingA,
            Function4<A, B, C, D, NewB> keyMappingB, Function4<A, B, C, D, NewC> keyMappingC,
            Function4<A, B, C, D, NewD> keyMappingD) {
        Variable<QuadTuple<NewA, NewB, NewC, NewD>> groupKey = internalsFactory.createVariable(QuadTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                createCompositeQuadGroupKey(keyMappingA, keyMappingB, keyMappingC, keyMappingD));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        Variable<NewD> newD = internalsFactory.createVariable("newD");
        IndirectPatternVariable<NewD, QuadTuple<NewA, NewB, NewC, NewD>> dPatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC, newD);
        return new QuadLeftHandSide<>(newA, newB, newC, dPatternVar, internalsFactory);
    }

    public <NewA> UniLeftHandSide<NewA> andMap(Function4<A, B, C, D, NewA> mapping) {
        Variable<NewA> newA = internalsFactory.createVariable("mapped", patternVariableA.getPrimaryVariable(),
                patternVariableB.getPrimaryVariable(), patternVariableC.getPrimaryVariable(),
                patternVariableD.getPrimaryVariable(), mapping);
        List<ViewItem<?>> allPrerequisites = mergeViewItems(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD);
        DirectPatternVariable<NewA> newPatternVariableA = new DirectPatternVariable<>(newA, allPrerequisites);
        return new UniLeftHandSide<>(newPatternVariableA, internalsFactory);
    }

    public <NewD> QuadLeftHandSide<A, B, C, NewD> andFlattenLast(Function1<D, Iterable<NewD>> mapping) {
        Variable<D> source = patternVariableD.getPrimaryVariable();
        Variable<NewD> newD = internalsFactory.createFlattenedVariable("flattened", source, mapping);
        List<ViewItem<?>> allPrerequisites = mergeViewItems(patternVariableA, patternVariableB, patternVariableC,
                patternVariableD);
        PatternVariable<NewD, ?, ?> newPatternVariableD = new DirectPatternVariable<>(newD, allPrerequisites);
        return new QuadLeftHandSide<>(patternVariableA.getPrimaryVariable(), patternVariableB.getPrimaryVariable(),
                patternVariableC.getPrimaryVariable(), newPatternVariableD, internalsFactory);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToIntQuadFunction<A, B, C, D> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToLongQuadFunction<A, B, C, D> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(QuadFunction<A, B, C, D, BigDecimal> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
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
