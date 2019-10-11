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

public final class RealUniAnchor extends UniAnchor<RealUniAnchor> {

    public RealUniAnchor(Declaration<?> aVariableDeclaration) {
        super(aVariableDeclaration);
    }

    private <A> RealUniAnchor(Declaration<A> aVariableDeclaration, PatternDSL.PatternDef<A> pattern) {
        super(aVariableDeclaration, pattern);
    }

    @Override
    public RealUniAnchor filter(Predicate predicate) {
        return new RealUniAnchor(getAVariableDeclaration(), getAPattern().expr(predicate::test));
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToIntFunction matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, a) -> {
                    RuleContext kcontext = (RuleContext) drools;
                    int weightMultiplier = matchWeighter.applyAsInt(a);
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToLongFunction matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, a) -> {
                    RuleContext kcontext = (RuleContext) drools;
                    long weightMultiplier = matchWeighter.applyAsLong(a);
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }

    @Override
    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, Function<Object, BigDecimal> matchWeighter) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, a) -> {
                    RuleContext kcontext = (RuleContext) drools;
                    BigDecimal weightMultiplier = matchWeighter.apply(a);
                    scoreHolder.impactScore(kcontext, weightMultiplier);
                });
    }
}
