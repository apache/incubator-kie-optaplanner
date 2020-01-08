/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.tri;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block5;
import org.drools.model.functions.Predicate4;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsCondition;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsPatternBuilder;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;
import org.optaplanner.core.impl.score.stream.drools.quad.DroolsQuadCondition;
import org.optaplanner.core.impl.score.stream.drools.quad.DroolsQuadRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniCondition;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;

import static org.drools.model.DSL.accFunction;
import static org.drools.model.DSL.from;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

public final class DroolsTriCondition<A, B, C> extends DroolsCondition<DroolsTriRuleStructure<A, B, C>> {

    public DroolsTriCondition(DroolsTriRuleStructure<A, B, C> ruleStructure) {
        super(ruleStructure);
    }

    public DroolsTriCondition<A, B, C> andFilter(TriPredicate<A, B, C> predicate) {
        Predicate4<Object, A, B, C> filter = (__, a, b, c) -> predicate.test(a, b, (C) c);
        Variable<A> aVariable = ruleStructure.getA();
        Variable<B> bVariable = ruleStructure.getB();
        Variable<C> cVariable = ruleStructure.getC();
        DroolsPatternBuilder<Object> newTargetPattern = ruleStructure.getPrimaryPattern()
                .expand(p -> p.expr("Filter using " + predicate, aVariable, bVariable, cVariable, filter));
        DroolsTriRuleStructure<A, B, C> newRuleStructure = new DroolsTriRuleStructure<>(aVariable, bVariable, cVariable,
                newTargetPattern, ruleStructure.getOpenRuleItems(), ruleStructure.getClosedRuleItems(),
                ruleStructure.getVariableIdSupplier());
        return new DroolsTriCondition<>(newRuleStructure);
    }

    public <D> DroolsQuadCondition<A, B, C, D> andJoin(DroolsUniCondition<D> dCondition,
            AbstractQuadJoiner<A, B, C, D> quadJoiner) {
        DroolsUniRuleStructure<D> dRuleStructure = dCondition.getRuleStructure();
        Variable<D> dVariable = dRuleStructure.getA();
        UnaryOperator<PatternDSL.PatternDef<Object>> expander =
                p -> p.expr("Filter using " + quadJoiner, ruleStructure.getA(), ruleStructure.getB(),
                        ruleStructure.getC(), dVariable, (__, a, b, c, d) -> matches(quadJoiner, a, b, c, d));
        DroolsUniRuleStructure<D> newDRuleStructure = dRuleStructure.amend(expander);
        return new DroolsQuadCondition<>(new DroolsQuadRuleStructure<>(ruleStructure, newDRuleStructure,
                ruleStructure.getVariableIdSupplier()));
    }

    public <ResultContainer, NewA, NewB, NewC> DroolsTriCondition<NewA, NewB, NewC> andGroupBiWithCollect(
            TriFunction<A, B, C, NewA> groupKeyAMapping, TriFunction<A, B, C, NewB> groupKeyBMapping,
            TriConstraintCollector<A, B, C, ResultContainer, NewC> collector) {
        Variable<Set<TriTuple<NewA, NewB, NewC>>> setOfPairsVar =
                (Variable<Set<TriTuple<NewA, NewB, NewC>>>) ruleStructure.createVariable(Set.class, "setOfTuples");
        PatternDSL.PatternDef<Set<TriTuple<NewA, NewB, NewC>>> pattern = pattern(setOfPairsVar)
                .expr("Set of resulting tuples", set -> !set.isEmpty(),
                        alphaIndexedBy(Integer.class, Index.ConstraintType.GREATER_THAN, -1, Set::size, 0));
        // Prepare the list of pairs.
        PatternDSL.PatternDef<Object> innerCollectingPattern = ruleStructure.getPrimaryPattern().build();
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(innerCollectingPattern);
        ViewItem<?> accumulate = DSL.accumulate(innerAccumulatePattern,
                accFunction(() -> new DroolsTriGroupByInvoker<>(groupKeyAMapping, groupKeyBMapping, collector,
                        getRuleStructure().getA(), getRuleStructure().getB(), getRuleStructure().getC()))
                        .as(setOfPairsVar));
        // Load one pair from the list.
        Variable<TriTuple<NewA, NewB, NewC>> oneTupleVar =
                (Variable<TriTuple<NewA, NewB, NewC>>) ruleStructure.createVariable(TriTuple.class, "tuple",
                        from(setOfPairsVar));
        DroolsTriRuleStructure<NewA, NewB, NewC> newRuleStructure = ruleStructure.regroupBiToTri(oneTupleVar, pattern,
                accumulate);
        return new DroolsTriCondition<>(newRuleStructure);
    }


    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b, c) -> impactScore(drools, scoreHolder));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToIntTriFunction<A, B, C> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b, c) -> impactScore(drools, scoreHolder, matchWeighter.applyAsInt(a, b, c)));

    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToLongTriFunction<A, B, C> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b, c) -> impactScore(drools, scoreHolder, matchWeighter.applyAsLong(a, b, c)));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            TriFunction<A, B, C, BigDecimal> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b, c) -> impactScore(drools, scoreHolder, matchWeighter.apply(a, b, c)));
    }

    private <ScoreHolder extends AbstractScoreHolder<?>> List<RuleItemBuilder<?>> completeWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block5<Drools, ScoreHolder, A, B, C> consequenceImpl) {
        ConsequenceBuilder._4<ScoreHolder, A, B, C> consequence =
                on(scoreHolderGlobal, ruleStructure.getA(), ruleStructure.getB(), ruleStructure.getC())
                        .execute(consequenceImpl);
        return ruleStructure.finish(consequence);
    }

    private static <A, B, C, D> boolean matches(AbstractQuadJoiner<A, B, C, D> joiner, A a, B b, C c, D d) {
        JoinerType[] joinerTypes = joiner.getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = joiner.getLeftMapping(i).apply(a, b, c);
            Object rightMapping = joiner.getRightMapping(i).apply(d);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

}
