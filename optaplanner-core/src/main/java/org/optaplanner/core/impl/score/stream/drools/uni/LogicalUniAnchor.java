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

public final class LogicalUniAnchor implements UniAnchor<LogicalUniAnchor> {

    private final String contextId = UniAnchor.createContextId();
    private final Declaration<LogicalTuple> aVariableDeclaration;
    private final PatternDSL.PatternDef<LogicalTuple> aPattern;

    public LogicalUniAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            BiFunction<String, Declaration<LogicalTuple>, PatternDSL.PatternDef<LogicalTuple>> patternProvider) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = patternProvider.apply(contextId, aVariableDeclaration);
    }

    private LogicalUniAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            PatternDSL.PatternDef<LogicalTuple> aPattern) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = aPattern;
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

    @Override
    public LogicalUniAnchor filter(Predicate predicate) {
        return new LogicalUniAnchor(aVariableDeclaration,
                aPattern.expr(logicalTuple -> predicate.test(logicalTuple.getItem(0))));
    }

    @Override
    public <A, GroupKey_> List<RuleItemBuilder<?>> terminateWithLogical(final String currentContextId,
            Function<A, GroupKey_> groupKeyMapping) {
        ConsequenceBuilder._1<LogicalTuple> consequence = on(getAVariableDeclaration())
                .execute((drools, logicalTuple) -> {
                    final A a = logicalTuple.getItem(0);
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
                PatternDSL.on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, logicalTuple) -> {
                            Object a = logicalTuple.getItem(0);
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
                PatternDSL.on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, logicalTuple) -> {
                            Object a = logicalTuple.getItem(0);
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
                PatternDSL.on(scoreHolderGlobal, aVariableDeclaration)
                        .execute((drools, scoreHolder, logicalTuple) -> {
                            Object a = logicalTuple.getItem(0);
                            BigDecimal weightMultiplier = matchWeighter.apply(a);
                            RuleContext kcontext = (RuleContext) drools;
                            scoreHolder.impactScore(kcontext, weightMultiplier);
                        });
        return Arrays.asList(getAPattern(), consequence);
    }
}
