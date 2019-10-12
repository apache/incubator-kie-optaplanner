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
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;
import org.optaplanner.core.impl.score.stream.drools.common.OriginalRuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.RuleMetadata;
import org.optaplanner.core.impl.score.stream.drools.uni.UniAnchor;

import static org.drools.model.DSL.on;

public final class BiAnchor {

    private final String contextId = UniAnchor.createContextId();
    private final RuleMetadata aMetadata;
    private final RuleMetadata bMetadata;

    public BiAnchor(RuleMetadata aMetadata, RuleMetadata bMetadata) {
        this.aMetadata = aMetadata;
        this.bMetadata = bMetadata;
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

    public BiAnchor filter(BiPredicate predicate) {
        RuleMetadata<?> bMetadata = getBMetadata();
        PatternDSL.PatternDef newPattern = bMetadata.getPattern()
                .expr(getAMetadata().getVariableDeclaration(),
                        (b, a) -> predicate.test(inline(a), inline(b)));
        if (bMetadata instanceof LogicalRuleMetadata) {
            return new BiAnchor(getAMetadata(), ((LogicalRuleMetadata) bMetadata).substitute(newPattern));
        } else {
            return new BiAnchor(getAMetadata(), ((OriginalRuleMetadata<?>) bMetadata).substitute(newPattern));
        }
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, __, ___) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a, b) -> {
                            int weightMultiplier = matchWeighter.applyAsInt(inline(a), inline(b));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), consequence);
    }

    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a, b) -> {
                            long weightMultiplier = matchWeighter.applyAsLong(inline(a), inline(b));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), consequence);
    }

    public <A, B> List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, getAMetadata().getVariableDeclaration(), getBMetadata().getVariableDeclaration())
                        .execute((drools, scoreHolder, a, b) -> {
                            BigDecimal weightMultiplier = matchWeighter.apply(inline(a), inline(b));
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAMetadata().getPattern(), getBMetadata().getPattern(), consequence);
    }

    public static <A, B> boolean matches(AbstractBiJoiner<A, B> biJoiner, A left, B right) {
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
