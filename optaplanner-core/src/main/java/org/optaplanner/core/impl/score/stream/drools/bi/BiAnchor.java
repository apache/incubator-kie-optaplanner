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
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import org.drools.model.Declaration;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block4;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.common.GenuineRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.InferredRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.RuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.tri.TriAnchor;
import org.optaplanner.core.impl.score.stream.drools.uni.UniAnchor;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

import static org.drools.model.DSL.on;

public final class BiAnchor {

    private final String contextId = UniAnchor.createContextId();
    private final RuleMetadata aMetadata;
    private final RuleMetadata bMetadata;
    private final Function aInliner;
    private final Function bInliner;

    public BiAnchor(RuleMetadata aMetadata, RuleMetadata bMetadata) {
        this.aMetadata = aMetadata;
        this.bMetadata = bMetadata;
        this.aInliner = UniAnchor.getInliner(aMetadata);
        this.bInliner = UniAnchor.getInliner(bMetadata);
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

    private <A> A inlineA(Object a) {
        return (A) aInliner.apply(a);
    }

    private <B> B inlineB(Object b) {
        return (B) bInliner.apply(b);
    }

    public BiAnchor filter(BiPredicate predicate) {
        RuleMetadata<?> bMetadata = getBMetadata();
        PatternDSL.PatternDef newPattern = bMetadata.getPattern()
                .expr(getAMetadata().getVariableDeclaration(), (b, a) -> predicate.test(inlineA(a), inlineB(b)));
        if (bMetadata instanceof InferredRuleMetadata) {
            return new BiAnchor(getAMetadata(), ((InferredRuleMetadata) bMetadata).substitute(newPattern));
        } else {
            return new BiAnchor(getAMetadata(), ((GenuineRuleMetadata<?>) bMetadata).substitute(newPattern));
        }
    }

    public <A, B, C> TriAnchor join(UniAnchor cAnchor, AbstractTriJoiner<A, B, C> triJoiner) {
        RuleMetadata<?> cMetadata = cAnchor.getAMetadata();
        Function cInliner = UniAnchor.getInliner(cMetadata);
        PatternDSL.PatternDef newPattern = cMetadata.getPattern()
                .expr(contextId, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration(),
                        (c, a, b) -> matches(triJoiner, inlineA(a), inlineB(b), (C) cInliner.apply(c)));
        if (cMetadata instanceof InferredRuleMetadata) {
            return new TriAnchor(getAMetadata(), getBMetadata(), ((InferredRuleMetadata) cMetadata).substitute(newPattern));
        } else {
            return new TriAnchor(getAMetadata(), getBMetadata(), ((GenuineRuleMetadata) cMetadata).substitute(newPattern));
        }
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, __, ___) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext);
        });
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntBiFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            int weightMultiplier = matchWeighter.applyAsInt(inlineA(a), inlineB(b));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongBiFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            long weightMultiplier = matchWeighter.applyAsLong(inlineA(a), inlineB(b));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public <A, B> List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            BiFunction<A, B, BigDecimal> matchWeighter) {
       return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b) -> {
            BigDecimal weightMultiplier = matchWeighter.apply(inlineA(a), inlineB(b));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    private <A, B, ScoreHolder extends AbstractScoreHolder> List<RuleItemBuilder<?>> terminateWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block4<Drools, ScoreHolder, A, B> consequenceImpl) {
        ConsequenceBuilder._3<ScoreHolder, A, B> consequence =
                on(scoreHolderGlobal, (Declaration<A>) getAMetadata().getVariableDeclaration(),
                        (Declaration<B>) getBMetadata().getVariableDeclaration())
                        .execute(consequenceImpl);
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), consequence);
    }

    private static <A, B, C> boolean matches(AbstractTriJoiner<A, B, C> triJoiner, A a, B b, C c) {
        Object[] leftMappings = triJoiner.getLeftCombinedMapping().apply(a, b);
        Object[] rightMappings = triJoiner.getRightCombinedMapping().apply(c);
        JoinerType[] joinerTypes = triJoiner.getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            if (!joinerType.matches(leftMappings[i], rightMappings[i])) {
                return false;
            }
        }
        return true;
    }

}
