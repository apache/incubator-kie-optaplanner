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
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;

import static org.drools.model.DSL.on;

public final class RealUniAnchor implements UniAnchor<RealUniAnchor> {

    private final String contextId = UniAnchor.createContextId();
    private final Declaration<?> aVariableDeclaration;
    private final PatternDSL.PatternDef<?> aPattern;

    public <A> RealUniAnchor(Class<A> aVariableType) {
        this(PatternDSL.declarationOf(aVariableType), (contextId, var) -> PatternDSL.pattern(var));
    }

    public <A> RealUniAnchor(Declaration<A> aVariableDeclaration,
            BiFunction<String, Declaration<A>, PatternDSL.PatternDef<A>> patternProvider) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = patternProvider.apply(contextId, aVariableDeclaration);
    }

    private <A> RealUniAnchor(Declaration<A> aVariableDeclaration, PatternDSL.PatternDef<A> aPattern) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = aPattern;
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

    @Override
    public RealUniAnchor filter(Predicate predicate) {
        return new RealUniAnchor(getAVariableDeclaration(), getAPattern().expr(predicate::test));
    }

    @Override
    public <A, GroupKey_> List<RuleItemBuilder<?>> terminateWithLogical(final String currentContextId,
            Function<A, GroupKey_> groupKeyMapping) {
        ConsequenceBuilder._1<A> consequence = on((Declaration<A>) getAVariableDeclaration())
                .execute((drools, a) -> {
                    final GroupKey_ aMapped = groupKeyMapping.apply(a);
                    RuleContext kcontext = (RuleContext) drools;
                    kcontext.insertLogical(new LogicalTuple(currentContextId, aMapped));
                });
        return Arrays.asList(aPattern, consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, __) -> {
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext);
                        });
        return Arrays.asList(getAPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToIntFunction matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, a) -> {
                            int weightMultiplier = matchWeighter.applyAsInt(a);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            ToLongFunction matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, a) -> {
                            long weightMultiplier = matchWeighter.applyAsLong(a);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), consequence);
    }

    @Override
    public List<RuleItemBuilder<?>> terminateWithScoring(Global<? extends AbstractScoreHolder> scoreHolderGlobal,
            Function<Object, BigDecimal> matchWeighter) {
        ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> consequence =
                on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, a) -> {
                            BigDecimal weightMultiplier = matchWeighter.apply(a);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), consequence);
    }
}
