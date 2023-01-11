package org.optaplanner.constraint.streams.drools.common;

import static java.util.Collections.singletonList;
import static org.drools.model.DSL.exists;
import static org.drools.model.DSL.not;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.BetaIndex;
import org.drools.model.DSL;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.Predicate2;
import org.drools.model.functions.accumulate.AccumulateFunction;
import org.drools.model.view.ViewItem;
import org.optaplanner.constraint.streams.common.bi.DefaultBiJoiner;
import org.optaplanner.constraint.streams.common.bi.FilteringBiJoiner;
import org.optaplanner.constraint.streams.drools.DroolsInternalsFactory;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.JoinerType;

/**
 * Represents the left-hand side of a Drools rule, the result of which is a single variable.
 * The simplest variant of such rule, with no filters or groupBys applied, would look like this in equivalent DRL:
 *
 * <pre>
 * {@code
 *  rule "Simplest univariate rule"
 *  when
 *      $a: Something()
 *  then
 *      // Do something with the $a variable.
 *  end
 * }
 * </pre>
 *
 * Left-hand side is that part of the rule between the "when" and "then" keywords.
 * The part between the "then" and "end" keywords is called the consequence of the rule, and this class does not represent it.
 * It can be created by calling e.g. {@link #andTerminate(ToIntFunction)}.
 *
 * There are also more complex variants of rules that still result in just one variable:
 *
 * <pre>
 * {@code
 *  rule "Complex univariate rule"
 *  when
 *      $accumulateResult: Collection() from accumulate(
 *          ...
 *      )
 *      $a: Object() from $accumulateResult
 *      exists Something()
 *  then
 *      // Do something with the $a variable.
 *  end
 * }
 * </pre>
 *
 * To create the simplest possible variant, call {@link #UniLeftHandSide(Class, DroolsInternalsFactory)}.
 * Further specializations can be introduced by calling builder methods such as {@link #andFilter(Predicate1)}.
 *
 * These builder methods will always return a new instance of {@link AbstractLeftHandSide}, as these are immutable.
 * Some builder methods, such as {@link #andJoin(UniLeftHandSide, BiJoiner)}, will return an instance of
 * {@link BiLeftHandSide} ({@link TriLeftHandSide}, ...), as that particular operation will increase the cardinality
 * of the parent constraint stream.
 *
 * @param <A> generic type of the resulting variable
 */
public final class UniLeftHandSide<A> extends AbstractLeftHandSide {

    private final PatternVariable<A, ?, ?> patternVariable;
    private final UniRuleContext<A> ruleContext;

    public UniLeftHandSide(Class<A> aClass, DroolsInternalsFactory internalsFactory) {
        this(new DirectPatternVariable<>(internalsFactory.createVariable(aClass, "var"), internalsFactory));
    }

    UniLeftHandSide(Variable<A> variable, List<ViewItem<?>> viewItems, DroolsInternalsFactory internalsFactory) {
        this(new DirectPatternVariable<>(variable, viewItems, internalsFactory));
    }

    UniLeftHandSide(PatternVariable<A, ?, ?> patternVariable) {
        super(patternVariable.getInternalsFactory());
        this.patternVariable = Objects.requireNonNull(patternVariable);
        this.ruleContext = buildRuleContext();
    }

    private UniRuleContext<A> buildRuleContext() {
        return new UniRuleContext<>(patternVariable.getPrimaryVariable(),
                patternVariable.build().toArray(new ViewItem<?>[0]));
    }

    public PatternVariable<A, ?, ?> getPatternVariableA() {
        return patternVariable;
    }

    public UniLeftHandSide<A> andFilter(Predicate1<A> predicate) {
        return new UniLeftHandSide<>(patternVariable.filter(predicate));
    }

    private <B> UniLeftHandSide<A> applyJoiners(Class<B> otherFactType, Predicate1<B> nullityFilter,
            DefaultBiJoiner<A, B> joiner, Predicate2<A, B> predicate, boolean shouldExist) {
        Variable<B> toExist = internalsFactory.createVariable(otherFactType, "toExist");
        PatternDSL.PatternDef<B> existencePattern = pattern(toExist);
        if (nullityFilter != null) {
            existencePattern = existencePattern.expr("Exclude nulls using " + nullityFilter, nullityFilter);
        }
        if (joiner == null) {
            return applyFilters(existencePattern, predicate, shouldExist);
        }
        int joinerCount = joiner.getJoinerCount();
        for (int mappingIndex = 0; mappingIndex < joinerCount; mappingIndex++) {
            JoinerType joinerType = joiner.getJoinerType(mappingIndex);
            Function1<A, Object> leftMapping = internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
            Function1<B, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
            Predicate2<B, A> joinPredicate = (b, a) -> joinerType.matches(leftMapping.apply(a), rightMapping.apply(b));
            existencePattern = existencePattern.expr("Join using joiner #" + mappingIndex + " in " + joiner,
                    patternVariable.getPrimaryVariable(), joinPredicate, createBetaIndex(joiner, mappingIndex));
        }
        return applyFilters(existencePattern, predicate, shouldExist);
    }

    private <B> BetaIndex<B, A, ?> createBetaIndex(DefaultBiJoiner<A, B> joiner, int mappingIndex) {
        JoinerType joinerType = joiner.getJoinerType(mappingIndex);
        Function1<A, Object> leftMapping = internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
        Function1<B, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
        return AbstractPatternVariable.createBetaIndex(joinerType, mappingIndex, leftMapping, rightMapping);
    }

    private <B> UniLeftHandSide<A> applyFilters(PatternDSL.PatternDef<B> existencePattern, Predicate2<A, B> predicate,
            boolean shouldExist) {
        PatternDSL.PatternDef<B> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, patternVariable.getPrimaryVariable(),
                        (b, a) -> predicate.test(a, b));
        ViewItem<?> existenceExpression = exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = not(possiblyFilteredExistencePattern);
        }
        return new UniLeftHandSide<>(patternVariable.addDependentExpression(existenceExpression));
    }

    private <B> UniLeftHandSide<A> existsOrNot(Class<B> bClass, BiJoiner<A, B>[] joiners, Predicate1<B> nullityFilter,
            boolean shouldExist) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern.
        DefaultBiJoiner<A, B> finalJoiner = null;
        Predicate2<A, B> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            BiJoiner<A, B> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof FilteringBiJoiner) {
                if (!hasAFilter) { // From now on, only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // Merge all filters into one to avoid paying the penalty for lack of indexing more than once.
                FilteringBiJoiner<A, B> castJoiner = (FilteringBiJoiner<A, B>) joiner;
                Predicate2<A, B> convertedFilter = internalsFactory.convert(castJoiner.getFilter());
                finalFilter = finalFilter == null ? convertedFilter : internalsFactory.merge(finalFilter, convertedFilter);
            } else {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    DefaultBiJoiner<A, B> castJoiner = (DefaultBiJoiner<A, B>) joiner;
                    finalJoiner = finalJoiner == null ? castJoiner : finalJoiner.and(castJoiner);
                }
            }
        }
        return applyJoiners(bClass, nullityFilter, finalJoiner, finalFilter, shouldExist);
    }

    public <B> UniLeftHandSide<A> andExists(Class<B> bClass, BiJoiner<A, B>[] joiners, Predicate1<B> nullityFilter) {
        return existsOrNot(bClass, joiners, nullityFilter, true);
    }

    public <B> UniLeftHandSide<A> andNotExists(Class<B> bClass, BiJoiner<A, B>[] joiners, Predicate1<B> nullityFilter) {
        return existsOrNot(bClass, joiners, nullityFilter, false);
    }

    public <B> BiLeftHandSide<A, B> andJoin(UniLeftHandSide<B> right, BiJoiner<A, B> joiner) {
        DefaultBiJoiner<A, B> castJoiner = (DefaultBiJoiner<A, B>) joiner;
        int joinerCount = castJoiner.getJoinerCount();
        PatternVariable<B, ?, ?> newRight = right.patternVariable;
        for (int mappingIndex = 0; mappingIndex < joinerCount; mappingIndex++) {
            JoinerType joinerType = castJoiner.getJoinerType(mappingIndex);
            newRight = newRight.filterForJoin(patternVariable.getPrimaryVariable(), castJoiner, joinerType, mappingIndex);
        }
        return new BiLeftHandSide<>(patternVariable, newRight);
    }

    public <NewA> UniLeftHandSide<NewA> andGroupBy(UniConstraintCollector<A, ?, NewA> collector) {
        Variable<NewA> accumulateOutput = internalsFactory.createVariable("collected");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collector, accumulateOutput));
        return new UniLeftHandSide<>(accumulateOutput, singletonList(outerAccumulatePattern), internalsFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(UniConstraintCollector<A, ?, NewA> collectorA,
            UniConstraintCollector<A, ?, NewB> collectorB) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB));
        return new BiLeftHandSide<>(accumulateOutputA,
                new DirectPatternVariable<>(accumulateOutputB, outerAccumulatePattern, internalsFactory));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(
            UniConstraintCollector<A, ?, NewA> collectorA, UniConstraintCollector<A, ?, NewB> collectorB,
            UniConstraintCollector<A, ?, NewC> collectorC) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("collectedC");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(accumulateOutputA, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, outerAccumulatePattern, internalsFactory));
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(
            UniConstraintCollector<A, ?, NewA> collectorA, UniConstraintCollector<A, ?, NewB> collectorB,
            UniConstraintCollector<A, ?, NewC> collectorC, UniConstraintCollector<A, ?, NewD> collectorD) {
        Variable<NewA> accumulateOutputA = internalsFactory.createVariable("collectedA");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("collectedB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("collectedC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("collectedD");
        ViewItem<?> outerAccumulatePattern = buildAccumulate(createAccumulateFunction(collectorA, accumulateOutputA),
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(accumulateOutputA, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, outerAccumulatePattern, internalsFactory));
    }

    /**
     * Creates a Drools accumulate function based on a given collector. The accumulate function will take
     * {@link PatternVariable}'s primary variable as input and return its result into another {@link Variable}.
     *
     * @param collector collector to use in the accumulate function
     * @param out variable in which to store accumulate result
     * @param <Out> type of the accumulate result
     * @return Drools accumulate function
     */
    private <Out> AccumulateFunction createAccumulateFunction(UniConstraintCollector<A, ?, Out> collector,
            Variable<Out> out) {
        Variable<A> variable = patternVariable.getPrimaryVariable();
        return new AccumulateFunction(null, () -> new UniAccumulator<>(variable, collector))
                .with(variable)
                .as(out);
    }

    public <NewA> UniLeftHandSide<NewA> andGroupBy(Function1<A, NewA> keyMapping) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMapping);
        return new UniLeftHandSide<>(groupKey, singletonList(groupByPattern), internalsFactory);
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(Function1<A, NewA> keyMappingA,
            UniConstraintCollector<A, ?, NewB> collectorB) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutput = internalsFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutput));
        return new BiLeftHandSide<>(groupKey, new DirectPatternVariable<>(accumulateOutput, groupByPattern, internalsFactory));
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function1<A, NewA> keyMappingA,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC));
        return new TriLeftHandSide<>(groupKey, accumulateOutputB,
                new DirectPatternVariable<>(accumulateOutputC, groupByPattern, internalsFactory));
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function1<A, NewA> keyMappingA,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        Variable<NewA> groupKey = internalsFactory.createVariable("groupKey");
        Variable<NewB> accumulateOutputB = internalsFactory.createVariable("outputB");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, keyMappingA,
                createAccumulateFunction(collectorB, accumulateOutputB),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        return new QuadLeftHandSide<>(groupKey, accumulateOutputB, accumulateOutputC,
                new DirectPatternVariable<>(accumulateOutputD, groupByPattern, internalsFactory));
    }

    public <NewA, NewB> BiLeftHandSide<NewA, NewB> andGroupBy(Function1<A, NewA> keyMappingA, Function1<A, NewB> keyMappingB) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey, a -> new BiTuple<>(keyMappingA.apply(a), keyMappingB.apply(a)));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        IndirectPatternVariable<NewB, BiTuple<NewA, NewB>> bPatternVar =
                decompose(groupKey, groupByPattern, newA, newB);
        return new BiLeftHandSide<>(newA, bPatternVar);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function1<A, NewA> keyMappingA,
            Function1<A, NewB> keyMappingB, UniConstraintCollector<A, ?, NewC> collectorC) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutput = internalsFactory.createVariable("output");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                a -> new BiTuple<>(keyMappingA.apply(a), keyMappingB.apply(a)),
                createAccumulateFunction(collectorC, accumulateOutput));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        DirectPatternVariable<NewC> cPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, accumulateOutput);
        return new TriLeftHandSide<>(newA, newB, cPatternVar);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function1<A, NewA> keyMappingA,
            Function1<A, NewB> keyMappingB, UniConstraintCollector<A, ?, NewC> collectorC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        Variable<BiTuple<NewA, NewB>> groupKey = internalsFactory.createVariable(BiTuple.class, "groupKey");
        Variable<NewC> accumulateOutputC = internalsFactory.createVariable("outputC");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                a -> new BiTuple<>(keyMappingA.apply(a), keyMappingB.apply(a)),
                createAccumulateFunction(collectorC, accumulateOutputC),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        DirectPatternVariable<NewD> dPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, accumulateOutputD);
        return new QuadLeftHandSide<>(newA, newB, accumulateOutputC, dPatternVar);
    }

    public <NewA, NewB, NewC> TriLeftHandSide<NewA, NewB, NewC> andGroupBy(Function1<A, NewA> keyMappingA,
            Function1<A, NewB> keyMappingB, Function1<A, NewC> keyMappingC) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey = internalsFactory.createVariable(TriTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                a -> new TriTuple<>(keyMappingA.apply(a), keyMappingB.apply(a), keyMappingC.apply(a)));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        IndirectPatternVariable<NewC, TriTuple<NewA, NewB, NewC>> cPatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC);
        return new TriLeftHandSide<>(newA, newB, cPatternVar);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function1<A, NewA> keyMappingA,
            Function1<A, NewB> keyMappingB, Function1<A, NewC> keyMappingC,
            UniConstraintCollector<A, ?, NewD> collectorD) {
        Variable<TriTuple<NewA, NewB, NewC>> groupKey = internalsFactory.createVariable(TriTuple.class, "groupKey");
        Variable<NewD> accumulateOutputD = internalsFactory.createVariable("outputD");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                a -> new TriTuple<>(keyMappingA.apply(a), keyMappingB.apply(a), keyMappingC.apply(a)),
                createAccumulateFunction(collectorD, accumulateOutputD));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        DirectPatternVariable<NewD> dPatternVar =
                decomposeWithAccumulate(groupKey, groupByPattern, newA, newB, newC, accumulateOutputD);
        return new QuadLeftHandSide<>(newA, newB, newC, dPatternVar);
    }

    public <NewA, NewB, NewC, NewD> QuadLeftHandSide<NewA, NewB, NewC, NewD> andGroupBy(Function1<A, NewA> keyMappingA,
            Function1<A, NewB> keyMappingB, Function1<A, NewC> keyMappingC, Function1<A, NewD> keyMappingD) {
        Variable<QuadTuple<NewA, NewB, NewC, NewD>> groupKey = internalsFactory.createVariable(QuadTuple.class, "groupKey");
        ViewItem<?> groupByPattern = buildGroupBy(groupKey,
                a -> new QuadTuple<>(keyMappingA.apply(a), keyMappingB.apply(a), keyMappingC.apply(a),
                        keyMappingD.apply(a)));
        Variable<NewA> newA = internalsFactory.createVariable("newA");
        Variable<NewB> newB = internalsFactory.createVariable("newB");
        Variable<NewC> newC = internalsFactory.createVariable("newC");
        Variable<NewD> newD = internalsFactory.createVariable("newD");
        IndirectPatternVariable<NewD, QuadTuple<NewA, NewB, NewC, NewD>> dPatternVar =
                decompose(groupKey, groupByPattern, newA, newB, newC, newD);
        return new QuadLeftHandSide<>(newA, newB, newC, dPatternVar);
    }

    public <NewA> UniLeftHandSide<NewA> andMap(Function1<A, NewA> mapping) {
        Variable<NewA> newA = internalsFactory.createVariable("mapped");
        PatternVariable<A, ?, ?> mappedVariable = patternVariable.bind(newA, mapping);
        IndirectPatternVariable<NewA, ?> newPatternVariableA;
        if (mappedVariable instanceof DirectPatternVariable) {
            newPatternVariableA = new IndirectPatternVariable<>((DirectPatternVariable<A>) mappedVariable, newA, mapping);
        } else if (mappedVariable instanceof IndirectPatternVariable) {
            newPatternVariableA = new IndirectPatternVariable<>((IndirectPatternVariable<A, ?>) mappedVariable, newA, mapping);
        } else {
            throw new IllegalStateException(
                    "Impossible state: Pattern variable is neither direct nor indirect: " + patternVariable);
        }
        return new UniLeftHandSide<>(newPatternVariableA);
    }

    public <NewA> UniLeftHandSide<NewA> andFlattenLast(Function1<A, Iterable<NewA>> mapping) {
        Variable<A> source = patternVariable.getPrimaryVariable();
        Variable<NewA> newA = internalsFactory.createFlattenedVariable("flattened", source, mapping);
        PatternVariable<NewA, ?, ?> newPatternVariableA =
                new DirectPatternVariable<>(newA, patternVariable.build(), internalsFactory);
        return new UniLeftHandSide<>(newPatternVariableA);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToIntFunction<A> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(ToLongFunction<A> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
    }

    public <Solution_> RuleBuilder<Solution_> andTerminate(Function<A, BigDecimal> matchWeigher) {
        return ruleContext.newRuleBuilder(matchWeigher);
    }

    private ViewItem<?> buildAccumulate(AccumulateFunction... accFunctions) {
        ViewItem<?> innerAccumulatePattern = joinViewItemsWithLogicalAnd(patternVariable);
        return buildAccumulate(innerAccumulatePattern, accFunctions);
    }

    private <GroupKey_> ViewItem<?> buildGroupBy(Variable<GroupKey_> groupKey,
            Function1<A, GroupKey_> groupKeyExtractor, AccumulateFunction... accFunctions) {
        Variable<A> input = patternVariable.getPrimaryVariable();
        ViewItem<?> innerGroupByPattern = joinViewItemsWithLogicalAnd(patternVariable);
        return DSL.groupBy(innerGroupByPattern, input, groupKey, groupKeyExtractor, accFunctions);
    }

}
