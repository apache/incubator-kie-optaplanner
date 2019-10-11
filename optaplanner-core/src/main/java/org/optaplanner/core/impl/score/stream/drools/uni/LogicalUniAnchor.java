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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.Declaration;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.common.LogicalTuple;

public final class LogicalUniAnchor extends UniAnchor<LogicalUniAnchor> {

    public LogicalUniAnchor(Declaration<LogicalTuple> aVariableDeclaration,
            BiFunction<String, Declaration<LogicalTuple>, PatternDSL.PatternDef<LogicalTuple>> patternProvider) {
        super(aVariableDeclaration, patternProvider);
    }

    private <A> LogicalUniAnchor(Declaration<A> aVariableDeclaration, PatternDSL.PatternDef<A> pattern) {
        super(aVariableDeclaration, pattern);
    }

    @Override
    public LogicalUniAnchor filter(Predicate predicate) {
        return new LogicalUniAnchor(getAVariableDeclaration(),
                getAPattern().expr(logicalTuple -> predicate.test(((LogicalTuple)logicalTuple).getItem(0))));
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToIntFunction matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, logicalTuple) -> {
                    Object a = ((LogicalTuple)logicalTuple).getItem(0);
                    int weightMultiplier = matchWeighter.applyAsInt(a);
                    RuleContext kcontext = (RuleContext) drools;
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToLongFunction matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, logicalTuple) -> {
                    Object a = ((LogicalTuple)logicalTuple).getItem(0);
                    long weightMultiplier = matchWeighter.applyAsLong(a);
                    RuleContext kcontext = (RuleContext) drools;
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, Function<Object, BigDecimal> matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, logicalTuple) -> {
                    Object a = ((LogicalTuple)logicalTuple).getItem(0);
                    BigDecimal weightMultiplier = matchWeighter.apply(a);
                    RuleContext kcontext = (RuleContext) drools;
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }
}
