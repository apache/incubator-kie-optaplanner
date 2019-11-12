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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block4;
import org.drools.model.functions.Predicate2;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriCondition;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniCondition;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

import static org.drools.model.DSL.on;

public final class DroolsBiCondition<A, B> {

    private final DroolsBiRuleStructure<A, B> ruleStructure;

    public DroolsBiCondition(DroolsBiRuleStructure<A, B> ruleStructure) {
        this.ruleStructure = ruleStructure;
    }

    public DroolsBiRuleStructure<A, B> getRuleStructure() {
        return ruleStructure;
    }

    public DroolsBiCondition<A, B> andFilter(BiPredicate<A, B> predicate) {
        Predicate2<Object, A> filter = (b, a) -> predicate.test(a, (B) b);
        Variable<A> aVariable = ruleStructure.getA();
        Variable<B> bVariable = ruleStructure.getB();
        Supplier<PatternDSL.PatternDef<?>> newTargetPattern = () -> ruleStructure.getTargetPattern()
                .expr("Filter using " + predicate, aVariable, filter);
        DroolsBiRuleStructure<A, B> newRuleStructure = new DroolsBiRuleStructure<>(aVariable, bVariable,
                newTargetPattern, ruleStructure.getSupportingRuleItems());
        return new DroolsBiCondition<>(newRuleStructure);
    }

    public <C> DroolsTriCondition<A, B, C> andJoin(DroolsUniCondition<C> cCondition,
            AbstractTriJoiner<A, B, C> triJoiner) {
        DroolsUniRuleStructure<C> cRuleStructure = cCondition.getARuleStructure();
        Variable<C> cVariable = cRuleStructure.getA();
        Supplier<PatternDSL.PatternDef<?>> cPattern = () -> cRuleStructure.getAPattern()
                .expr("Filter using " + triJoiner, ruleStructure.getA(), ruleStructure.getB(),
                        (c, a, b) -> matches(triJoiner, a, b, (C) c));
        DroolsUniRuleStructure<C> newCRuleStructure = new DroolsUniRuleStructure<>(cVariable, cPattern,
                cRuleStructure.getSupportingRuleItems());
        return new DroolsTriCondition<>(new DroolsTriRuleStructure<>(ruleStructure, newCRuleStructure));

    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, __, ___) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext);
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToIntBiFunction<A, B> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.applyAsInt(a, b));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToLongBiFunction<A, B> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.applyAsLong(a, b));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, matchWeighter.apply(a, b));
        });
    }

    private <ScoreHolder extends AbstractScoreHolder<?>> List<RuleItemBuilder<?>> completeWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block4<Drools, ScoreHolder, A, B> consequenceImpl) {
        ConsequenceBuilder._3<ScoreHolder, A, B> consequence =
                on(scoreHolderGlobal, ruleStructure.getA(), ruleStructure.getB())
                        .execute(consequenceImpl);
        return rebuildRuleItems(ruleStructure, ruleStructure.getTargetPattern(), consequence);
    }

    private List<RuleItemBuilder<?>> rebuildRuleItems(DroolsBiRuleStructure<A, B> ruleStructure,
            RuleItemBuilder<?>... toAdd) {
        List<RuleItemBuilder<?>> supporting = new ArrayList<>(ruleStructure.getSupportingRuleItems());
        for (RuleItemBuilder<?> ruleItem : toAdd) {
            supporting.add(ruleItem);
        }
        return supporting;
    }

    private static <A, B, C> boolean matches(AbstractTriJoiner<A, B, C> triJoiner, A a, B b, C c) {
        JoinerType[] joinerTypes = triJoiner.getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = triJoiner.getLeftMapping(i).apply(a, b);
            Object rightMapping = triJoiner.getRightMapping(i).apply(c);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

}
