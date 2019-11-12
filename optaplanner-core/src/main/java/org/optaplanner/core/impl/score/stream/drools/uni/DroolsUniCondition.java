/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.core.base.accumulators.CollectSetAccumulateFunction;
import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block3;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate2;
import org.drools.model.functions.Predicate3;
import org.drools.model.view.ExprViewItem;
import org.kie.api.runtime.rule.AccumulateFunction;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.bi.DroolsBiCondition;
import org.optaplanner.core.impl.score.stream.drools.bi.DroolsBiRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAccumulateContext;

import static org.drools.model.DSL.accFunction;
import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.from;
import static org.drools.model.PatternDSL.pattern;

public final class DroolsUniCondition<A> {

    private final DroolsUniRuleStructure<A> aRuleStructure;

    public DroolsUniCondition(Class<A> aVariableType) {
        this.aRuleStructure = new DroolsUniRuleStructure<>(aVariableType);
    }

    public DroolsUniRuleStructure<A> getARuleStructure() {
        return aRuleStructure;
    }

    public DroolsUniCondition(DroolsUniRuleStructure<A> aRuleStructure) {
        this.aRuleStructure = aRuleStructure;
    }

    public DroolsUniCondition<A> andFilter(Predicate<A> predicate) {
        Predicate2<Object, A> filter = (__, a) -> predicate.test(a);
        DroolsUniRuleStructure<A> newStructure = new DroolsUniRuleStructure<>(aRuleStructure.getA(),
                () -> aRuleStructure.getAPattern().expr("Filter using " + predicate, aRuleStructure.getA(), filter),
                aRuleStructure.getSupportingRuleItems());
        return new DroolsUniCondition<>(newStructure);
    }

    public <NewA, ResultContainer> DroolsUniCondition<NewA> andCollect(
            UniConstraintCollector<A, ResultContainer, NewA> collector) {
        Variable<A> inputVariable = aRuleStructure.getA();
        PatternDSL.PatternDef<Object> innerAccumulatePattern = aRuleStructure.getAPattern();
        AccumulateFunction<DroolsAccumulateContext<ResultContainer>> accumulateFunction =
                new DroolsUniAccumulateFunctionBridge<>(collector);
        Variable<NewA> outputVariable = (Variable<NewA>) declarationOf(Object.class);
        Supplier<PatternDSL.PatternDef<?>> accumulateResult = () -> pattern(outputVariable);
        ExprViewItem<Object> outerAccumulatePattern = DSL.accumulate(innerAccumulatePattern,
                accFunction(() -> accumulateFunction, inputVariable).as(outputVariable));
        DroolsUniRuleStructure<NewA> newRuleStructure = new DroolsUniRuleStructure<>(outputVariable,
                accumulateResult, rebuildRuleItems(aRuleStructure, outerAccumulatePattern));
        return new DroolsUniCondition<>(newRuleStructure);
    }

    private List<RuleItemBuilder<?>> rebuildRuleItems(DroolsUniRuleStructure<A> ruleStructure,
            RuleItemBuilder<?>... toAdd) {
        List<RuleItemBuilder<?>> supporting = new ArrayList<>(ruleStructure.getSupportingRuleItems());
        for (RuleItemBuilder<?> ruleItem : toAdd) {
            supporting.add(ruleItem);
        }
        return supporting;
    }

    public <NewA> DroolsUniCondition<NewA> andGroup(Function<A, NewA> groupKeyMapping) {
        Variable<NewA> mappedVariable = aRuleStructure.createVariable("mapped");
        PatternDSL.PatternDef<Object> innerAccumulatePattern = aRuleStructure.getAPattern()
                .bind(mappedVariable, k -> groupKeyMapping.apply((A) k));
        Variable<Set> setOfGroupKeys = aRuleStructure.createVariable(Set.class,"setOfGroupKey");
        PatternDSL.PatternDef<Set> pattern = pattern(setOfGroupKeys)
                .expr("Set of groupKey", set -> !set.isEmpty(),
                        PatternDSL.alphaIndexedBy(Boolean.class, Index.ConstraintType.EQUAL, -1, Set::isEmpty, false));
        ExprViewItem<Object> accumulate = DSL.accumulate(innerAccumulatePattern,
                accFunction(CollectSetAccumulateFunction.class, mappedVariable).as(setOfGroupKeys));
        Variable<NewA> groupKey = aRuleStructure.createVariable("groupKey", from(setOfGroupKeys));
        Supplier<PatternDSL.PatternDef<?>> finalGroupKeyPattern = () -> pattern(groupKey);
        DroolsUniRuleStructure<NewA> newRuleStructure = new DroolsUniRuleStructure<>(groupKey,
                finalGroupKeyPattern, rebuildRuleItems(aRuleStructure, pattern, accumulate));
        return new DroolsUniCondition<>(newRuleStructure);
    }

    /**
     * The goal of this method is to create the left-hand side of a rule to look like this:
     *
     * <pre>
     * when
     *     $list(size > 0): accumulate(Person(), $list: Pair.of(Person::getCity, ConstraintCollectors.count()))
     *     Pair($newA: left, $newB: right) from $list
     * then
     *     ...
     * end
     * </pre>
     * <p>
     * Note: This is pseudo-code and the actual Drools code will look slightly different in terms of syntax.
     * @param groupKeyMapping never null, grouping to apply
     * @param collector never null, collector to apply
     * @param <ResultContainer> implementation detail, unimportant
     * @param <NewA> type of the first logical fact
     * @param <NewB> type of the second logical fact
     * @return
     */
    public <ResultContainer, NewA, NewB> DroolsBiCondition<NewA, NewB> andGroupWithCollect(
            Function<A, NewA> groupKeyMapping, UniConstraintCollector<A, ResultContainer, NewB> collector) {
        Variable<A> collectingOnVar = aRuleStructure.createVariable(aRuleStructure.getA().getType(), "collectingOn");
        Variable<NewA> groupKeyVar = aRuleStructure.createVariable("groupKey");
        Variable<List> listOfPairsVar = aRuleStructure.createVariable(List.class,"listOfPairs");
        // Prepare the list of pairs.
        PatternDSL.PatternDef<List> pattern = pattern(listOfPairsVar)
                .expr("List of groupBy+collect pairs", list -> !list.isEmpty(),
                        PatternDSL.alphaIndexedBy(Boolean.class, Index.ConstraintType.EQUAL, -1, List::isEmpty, false));
        PatternDSL.PatternDef<Object> innerNewACollectingPattern = aRuleStructure.getAPattern()
                .bind(groupKeyVar, a -> groupKeyMapping.apply((A) a))
                .bind(collectingOnVar, a -> (A) a);
        ExprViewItem<Object> accumulate = DSL.accumulate(innerNewACollectingPattern,
                accFunction(() -> new DroolsGroupByInvoker<>(collector)).as(listOfPairsVar));
        // Load one pair from the list.
        Variable<DroolsGroupByAccumulator.Pair> onePairVar = aRuleStructure.createVariable(
                DroolsGroupByAccumulator.Pair.class, "pair", from(listOfPairsVar));
        Variable<NewA> newAVar = aRuleStructure.createVariable("newA");
        Variable<NewB> newBVar = aRuleStructure.createVariable("newB");
        Supplier<PatternDSL.PatternDef<?>> finalPairPattern = () -> pattern(onePairVar)
                .bind(newAVar, pair -> (NewA) pair.getKey(), PatternDSL.reactOn("key"))
                .bind(newBVar, pair -> (NewB) pair.getValue(), PatternDSL.reactOn("value"));
        DroolsBiRuleStructure<NewA, NewB> newRuleStructure = new DroolsBiRuleStructure<>(newAVar, newBVar,
                finalPairPattern, rebuildRuleItems(aRuleStructure, pattern, accumulate));
        return new DroolsBiCondition<>(newRuleStructure);
    }

    public <B> DroolsBiCondition<A, B> andJoin(DroolsUniCondition<B> bCondition, AbstractBiJoiner<A, B> biJoiner) {
        DroolsUniRuleStructure<B> bRuleStructure = bCondition.aRuleStructure;
        Variable<B> bVariable = bRuleStructure.getA();
        Supplier<PatternDSL.PatternDef<?>> newBPattern = () -> {
            PatternDSL.PatternDef bPattern = bRuleStructure.getAPattern();
            JoinerType[] joinerTypes = biJoiner.getJoinerTypes();
            for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
                bPattern = join(bVariable, bPattern, biJoiner, mappingIndex);
            }
            return bPattern;
        };
        DroolsUniRuleStructure<B> newBRuleStructure = new DroolsUniRuleStructure<>(bVariable, newBPattern,
                bRuleStructure.getSupportingRuleItems());
        return new DroolsBiCondition<>(new DroolsBiRuleStructure<>(aRuleStructure, newBRuleStructure));
    }

    private <B> PatternDSL.PatternDef<Object> join(Variable<B> bVariable, PatternDSL.PatternDef<Object> bPattern,
            AbstractBiJoiner<A, B> biJoiner, int mappingIndex) {
        JoinerType joinerType = biJoiner.getJoinerTypes()[mappingIndex];
        Function<A, Object> leftMapping = biJoiner.getLeftMapping(mappingIndex);
        Function<B, Object> rightMapping = biJoiner.getRightMapping(mappingIndex);
        Function1<A, Object> leftExtractor = leftMapping::apply;
        Function1<B, Object> rightExtractor = rightMapping::apply;
        Predicate3<Object, A, B> predicate = (__, a, b) -> {
            Object left = leftExtractor.apply(a);
            Object right = rightExtractor.apply(b);
            return joinerType.matches(left, right);
        };
        return bPattern.expr("Join using joiner #" + mappingIndex + " in " + biJoiner, aRuleStructure.getA(), bVariable,
                predicate);
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, __) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext);
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToIntFunction<A> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.applyAsInt(a));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToLongFunction<A> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.applyAsLong(a));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            Function<A, BigDecimal> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.apply(a));
        });
    }

    private <ScoreHolder extends AbstractScoreHolder<?>> List<RuleItemBuilder<?>> completeWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block3<Drools, ScoreHolder, A> consequenceImpl) {
        ConsequenceBuilder._2<ScoreHolder, A> consequence = on(scoreHolderGlobal, aRuleStructure.getA())
                .execute(consequenceImpl);
        return rebuildRuleItems(aRuleStructure, aRuleStructure.getAPattern(), consequence);
    }
}
