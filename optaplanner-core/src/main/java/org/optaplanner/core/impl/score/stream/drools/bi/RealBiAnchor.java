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

import org.drools.model.Declaration;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

import static org.drools.model.DSL.on;

public final class RealBiAnchor implements BiAnchor<RealBiAnchor> {

    private final String contextId = BiAnchor.createContextId();
    private final Declaration<?> aVariableDeclaration;
    private final PatternDSL.PatternDef<?> aPattern;
    private final Declaration<?> bVariableDeclaration;
    private final PatternDSL.PatternDef<?> bPattern;

    public <A, B> RealBiAnchor(Declaration<A> aVariableDeclaration, PatternDSL.PatternDef<A> aPattern,
            Declaration<B> bVariableDeclaration, PatternDSL.PatternDef<B> bPattern) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = aPattern;
        this.bVariableDeclaration = bVariableDeclaration;
        this.bPattern = bPattern;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    public <A> Declaration<A> getAVariableDeclaration() {
        return (Declaration<A>) aVariableDeclaration;
    }

    public <A> PatternDSL.PatternDef<A> getAPattern() {
        return (PatternDSL.PatternDef<A>) aPattern;
    }

    public <B> Declaration<B> getBVariableDeclaration() {
        return (Declaration<B>) bVariableDeclaration;
    }

    public <B> PatternDSL.PatternDef<B>  getBPattern() {
        return (PatternDSL.PatternDef<B>) bPattern;
    }

    @Override
    public RealBiAnchor filter(BiPredicate predicate) {
        return new RealBiAnchor(getAVariableDeclaration(), getAPattern(), getBVariableDeclaration(),
                getBPattern().expr(getAVariableDeclaration(), (b, a) -> predicate.test(a, b)));
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, __, ___) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(getAPattern(), getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, a, b) -> {
                            int weightMultiplier = matchWeighter.applyAsInt(a, b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, a, b) -> {
                            long weightMultiplier = matchWeighter.applyAsLong(a, b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            BiFunction<Object, Object, BigDecimal> matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, a, b) -> {
                            BigDecimal weightMultiplier = matchWeighter.apply(a, b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), getBPattern(), consequence);
    }
}
