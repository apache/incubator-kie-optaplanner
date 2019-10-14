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

package org.optaplanner.core.impl.score.stream.drools.tri;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.drools.model.Declaration;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block5;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.common.InferredRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;
import org.optaplanner.core.impl.score.stream.drools.common.GenuineRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.RuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.uni.UniAnchor;

import static org.drools.model.DSL.on;

public final class TriAnchor {

    private final String contextId = UniAnchor.createContextId();
    private final RuleMetadata aMetadata;
    private final RuleMetadata bMetadata;
    private final RuleMetadata cMetadata;

    public TriAnchor(RuleMetadata aMetadata, RuleMetadata bMetadata, RuleMetadata cMetadata) {
        this.aMetadata = aMetadata;
        this.bMetadata = bMetadata;
        this.cMetadata = cMetadata;
    }

    public String getContextId() {
        return contextId;
    }

    public <X extends RuleMetadata<A>, A> X getAMetadata() {
        return (X) aMetadata;
    }

    public <X extends RuleMetadata<B>, B> X getBMetadata() {
        return (X) bMetadata;
    }

    public <X extends RuleMetadata<C>, C> X getCMetadata() {
        return (X) cMetadata;
    }

    public TriAnchor filter(TriPredicate predicate) {
        PatternDSL.PatternDef newPattern = getCMetadata().getPattern()
                .expr(contextId, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration(),
                        (c, a, b) -> predicate.test(inline(a), inline(b), inline(c)));
        if (bMetadata instanceof InferredRuleMetadata) {
            return new TriAnchor(getAMetadata(), getBMetadata(),
                    ((InferredRuleMetadata) cMetadata).substitute(newPattern));
        } else {
            return new TriAnchor(getAMetadata(), getBMetadata(),
                    ((GenuineRuleMetadata<?>) cMetadata).substitute(newPattern));
        }
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, __, ___) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), getCMetadata().getPattern(),
                consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntTriFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c) -> {
            int weightMultiplier = matchWeighter.applyAsInt(inline(a), inline(b), inline(c));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongTriFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c) -> {
            long weightMultiplier = matchWeighter.applyAsLong(inline(a), inline(b), inline(c));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public <A, B, C> List<RuleItemBuilder<?>> terminateWithScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, TriFunction<A, B, C, BigDecimal> matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c) -> {
            BigDecimal weightMultiplier = matchWeighter.apply(inline(a), inline(b), inline(c));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    private <A, B, C, ScoreHolder extends AbstractScoreHolder> List<RuleItemBuilder<?>> terminateWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block5<Drools, ScoreHolder, A, B, C> consequenceImpl) {
        ConsequenceBuilder._4<ScoreHolder, A, B, C> consequence =
                on(scoreHolderGlobal, (Declaration<A>) getAMetadata().getVariableDeclaration(),
                        (Declaration<B>) getBMetadata().getVariableDeclaration(),
                        (Declaration<C>) getCMetadata().getVariableDeclaration())
                        .execute(consequenceImpl);
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), getCMetadata().getPattern(),
                consequence);
    }

    private static <A> A inline(Object item) {
        if (item instanceof LogicalTuple) {
            return ((LogicalTuple) item).getItem(0);
        }
        return (A) item;
    }

}
