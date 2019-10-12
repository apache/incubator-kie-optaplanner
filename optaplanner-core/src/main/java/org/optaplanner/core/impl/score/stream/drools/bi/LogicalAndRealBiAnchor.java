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
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;

import static org.drools.model.DSL.on;

public final class LogicalAndRealBiAnchor implements BiAnchor<LogicalAndRealBiAnchor> {

    private final String contextId = BiAnchor.createContextId();
    private final Declaration<LogicalTuple> aVariableDeclaration;
    private final PatternDSL.PatternDef<LogicalTuple> aPattern;
    private final Declaration<?> bVariableDeclaration;
    private final PatternDSL.PatternDef<?> bPattern;

    public <B> LogicalAndRealBiAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            PatternDSL.PatternDef<LogicalTuple> aPattern, Declaration<B> bVariableDeclaration,
            PatternDSL.PatternDef<B> bPattern) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = aPattern;
        this.bVariableDeclaration = bVariableDeclaration;
        this.bPattern = bPattern;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    public Declaration<LogicalTuple> getAVariableDeclaration() {
        return aVariableDeclaration;
    }

    public PatternDSL.PatternDef<LogicalTuple> getAPattern() {
        return aPattern;
    }

    public <B> Declaration<B> getBVariableDeclaration() {
        return (Declaration<B>) bVariableDeclaration;
    }

    public <B> PatternDSL.PatternDef<B> getBPattern() {
        return (PatternDSL.PatternDef<B>) bPattern;
    }

    @Override
    public LogicalAndRealBiAnchor filter(BiPredicate predicate) {
        return new LogicalAndRealBiAnchor(aVariableDeclaration, aPattern, getBVariableDeclaration(),
                getBPattern().expr(aVariableDeclaration, (b, logicalA) -> predicate.test(logicalA.getItem(0), b)));
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, __, ___) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(aPattern, getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, aLogical, b) -> {
                            int weightMultiplier = matchWeighter.applyAsInt(aLogical.getItem(0), b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(aPattern, getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongBiFunction matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, aLogical, b) -> {
                            long weightMultiplier = matchWeighter.applyAsLong(aLogical.getItem(0), b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(aPattern, getBPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            BiFunction<Object, Object, BigDecimal> matchWeighter) {
        ConsequenceBuilder._3<? extends AbstractScoreHolder, ?, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration, bVariableDeclaration)
                        .execute((drools, scoreHolder, aLogical, b) -> {
                            BigDecimal weightMultiplier = matchWeighter.apply(aLogical.getItem(0), b);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(aPattern, getBPattern(), consequence);
    }
}
