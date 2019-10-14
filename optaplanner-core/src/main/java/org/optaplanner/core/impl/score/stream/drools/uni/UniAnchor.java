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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.Declaration;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block3;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.bi.BiAnchor;
import org.optaplanner.core.impl.score.stream.drools.common.GenuineRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.InferredRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;
import org.optaplanner.core.impl.score.stream.drools.common.RuleMetadata;

import static org.drools.model.DSL.on;

public final class UniAnchor {

    private static final Function GENUINE_INLINER = Function.identity();
    private static final Function INFERRED_INLINER = logicalTuple -> ((LogicalTuple)logicalTuple).getItem(0);

    private final String contextId = createContextId();
    private final RuleMetadata aMetadata;
    private final Function aInliner;

    public <A> UniAnchor(Class<A> aVariableType) {
        Declaration<A> aVariableDeclaration = PatternDSL.declarationOf(aVariableType);
        this.aMetadata = RuleMetadata.of(aVariableDeclaration, PatternDSL.pattern(aVariableDeclaration));
        this.aInliner = GENUINE_INLINER;
    }

    public UniAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            BiFunction<String, Declaration<LogicalTuple>, PatternDSL.PatternDef<LogicalTuple>> patternProvider) {
        this.aMetadata = RuleMetadata.ofInferred(aVariableDeclaration,
                patternProvider.apply(contextId, aVariableDeclaration));
        this.aInliner = INFERRED_INLINER;
    }

    private <A> UniAnchor(RuleMetadata<A> aMetadata) {
        this.aMetadata = aMetadata;
        this.aInliner = getInliner(aMetadata);
    }

    private <A> A inlineA(Object a) {
        return (A) aInliner.apply(a);
    }

    public String getContextId() {
        return contextId;
    }

    public <X extends RuleMetadata<A>, A> X getAMetadata() {
        return (X) aMetadata;
    }

    public UniAnchor filter(Predicate predicate) {
        PatternDSL.PatternDef newPattern = getAMetadata().getPattern().expr(a -> predicate.test(inlineA(a)));
        if (aMetadata instanceof InferredRuleMetadata) {
            return new UniAnchor(((InferredRuleMetadata) aMetadata).substitute(newPattern));
        } else {
            return new UniAnchor(((GenuineRuleMetadata) aMetadata).substitute(newPattern));
        }
    }

    public <A, B> BiAnchor join(UniAnchor bAnchor, AbstractBiJoiner<A, B> biJoiner) {
        RuleMetadata<?> bMetadata = bAnchor.getAMetadata();
        Function bInliner = getInliner(bMetadata);
        PatternDSL.PatternDef newPattern = bMetadata.getPattern()
                .expr(getAMetadata().getVariableDeclaration(),
                        (b, a) -> matches(biJoiner, inlineA(a), (B) bInliner.apply(b)));
        if (bMetadata instanceof InferredRuleMetadata) {
            return new BiAnchor(getAMetadata(), ((InferredRuleMetadata) bMetadata).substitute(newPattern));
        } else {
            return new BiAnchor(getAMetadata(), ((GenuineRuleMetadata) bMetadata).substitute(newPattern));
        }
    }

    public <A, GroupKey_> List<RuleItemBuilder<?>> terminateWithLogical(final String currentContextId,
            Function<A, GroupKey_> groupKeyMapping) {
        ConsequenceBuilder._1<?> consequence = on(getAMetadata().getVariableDeclaration())
                .execute((drools, a) -> {
                    final A aInlined = inlineA(a);
                    final GroupKey_ aMapped = groupKeyMapping.apply(aInlined);
                    RuleContext kcontext = (RuleContext) drools;
                    kcontext.insertLogical(new LogicalTuple(currentContextId, aMapped));
                });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, __) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext);
        });
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            int weightMultiplier = matchWeighter.applyAsInt(inlineA(a));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongFunction matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            long weightMultiplier = matchWeighter.applyAsLong(inlineA(a));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    public <A> List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            Function<A, BigDecimal> matchWeighter) {
        return terminateWithScoring(scoreHolderGlobal, (drools, scoreHolder, a) -> {
            BigDecimal weightMultiplier = matchWeighter.apply(inlineA(a));
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier);
        });
    }

    private <A, ScoreHolder extends AbstractScoreHolder> List<RuleItemBuilder<?>> terminateWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block3<Drools, ScoreHolder, A> consequenceImpl) {
        ConsequenceBuilder._2<ScoreHolder, A> consequence =
                on(scoreHolderGlobal, (Declaration<A>) getAMetadata().getVariableDeclaration())
                        .execute(consequenceImpl);
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public static String createContextId() {
        return UUID.randomUUID().toString();
    }

    public static Function getInliner(RuleMetadata<?> ruleMetadata) {
        return (ruleMetadata instanceof GenuineRuleMetadata) ? GENUINE_INLINER : INFERRED_INLINER;
    }

    private static <A, B> boolean matches(AbstractBiJoiner<A, B> biJoiner, A left, B right) {
        Object[] leftMappings = biJoiner.getLeftCombinedMapping().apply(left);
        Object[] rightMappings = biJoiner.getRightCombinedMapping().apply(right);
        JoinerType[] joinerTypes = biJoiner.getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            if (!joinerType.matches(leftMappings[i], rightMappings[i])) {
                return false;
            }
        }
        return true;
    }

}
