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
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.bi.BiAnchor;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;
import org.optaplanner.core.impl.score.stream.drools.common.OriginalRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.RuleMetadata;

import static org.drools.model.DSL.on;

public final class UniAnchor {

    private final String contextId = createContextId();
    private final RuleMetadata aMetadata;

    public <A> UniAnchor(Class<A> aVariableType) {
        Declaration<A> aVariableDeclaration = PatternDSL.declarationOf(aVariableType);
        this.aMetadata = RuleMetadata.of(aVariableDeclaration, PatternDSL.pattern(aVariableDeclaration));
    }

    public UniAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            BiFunction<String, Declaration<LogicalTuple>, PatternDSL.PatternDef<LogicalTuple>> patternProvider) {
        this.aMetadata = RuleMetadata.ofLogical(aVariableDeclaration,
                patternProvider.apply(contextId, aVariableDeclaration));
    }

    private <A> UniAnchor(RuleMetadata<A> aMetadata) {
        this.aMetadata = aMetadata;
    }

    public String getContextId() {
        return contextId;
    }

    public <X extends RuleMetadata<A>, A> X getAMetadata() {
        return (X) aMetadata;
    }

    public UniAnchor filter(Predicate predicate) {
        PatternDSL.PatternDef newPattern = getAMetadata().getPattern().expr(a -> predicate.test(inline(a)));
        if (aMetadata instanceof LogicalRuleMetadata) {
            return new UniAnchor(((LogicalRuleMetadata) aMetadata).substitute(newPattern));
        } else {
            return new UniAnchor(((OriginalRuleMetadata) aMetadata).substitute(newPattern));
        }
    }

    public <A, B> BiAnchor join(UniAnchor bAnchor, AbstractBiJoiner<A, B> biJoiner) {
        RuleMetadata<?> bMetadata = bAnchor.getAMetadata();
        PatternDSL.PatternDef newPattern = bMetadata.getPattern()
                .expr(getAMetadata().getVariableDeclaration(),
                        (b, a) -> matches(biJoiner, inline(a), inline(b)));
        if (bMetadata instanceof LogicalRuleMetadata) {
            return new BiAnchor(getAMetadata(), ((LogicalRuleMetadata) bMetadata).substitute(newPattern));
        } else {
            return new BiAnchor(getAMetadata(), ((OriginalRuleMetadata) bMetadata).substitute(newPattern));
        }
    }

    public <A, GroupKey_> List<RuleItemBuilder<?>> terminateWithLogical(final String currentContextId,
            Function<A, GroupKey_> groupKeyMapping) {
        ConsequenceBuilder._1<?> consequence = on(getAMetadata().getVariableDeclaration())
                .execute((drools, a) -> {
                    final A aInlined = inline(a);
                    final GroupKey_ aMapped = groupKeyMapping.apply(aInlined);
                    RuleContext kcontext = (RuleContext) drools;
                    kcontext.insertLogical(new LogicalTuple(currentContextId, aMapped));
                });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, __) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntFunction matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a) -> {
                            int weightMultiplier = matchWeighter.applyAsInt(inline(a));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongFunction matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a) -> {
                            long weightMultiplier = matchWeighter.applyAsLong(inline(a));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public <A> List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            Function<A, BigDecimal> matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a) -> {
                            BigDecimal weightMultiplier = matchWeighter.apply(inline(a));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), consequence);
    }

    public static String createContextId() {
        return UUID.randomUUID().toString();
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

    private static <A> A inline(Object item) {
        if (item instanceof LogicalTuple) {
            return ((LogicalTuple) item).getItem(0);
        }
        return (A) item;
    }

}
