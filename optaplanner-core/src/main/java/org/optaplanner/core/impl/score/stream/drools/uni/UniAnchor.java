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
import java.util.UUID;
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

public abstract class UniAnchor<T extends UniAnchor<T>> {

    private final Declaration<?> aVariableDeclaration;
    private final PatternDSL.PatternDef<?> aPattern;
    private final String contextId = UUID.randomUUID().toString();

    protected <A> UniAnchor(Declaration<A> aVariableDeclaration) {
        this(aVariableDeclaration, (contextId, var) -> PatternDSL.pattern(var));
    }

    protected <A> UniAnchor(Declaration<A> aVariableDeclaration, BiFunction<String, Declaration<A>,
                PatternDSL.PatternDef<A>> patternProvider) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = patternProvider.apply(contextId, aVariableDeclaration);
    }

    protected <A> UniAnchor(Declaration<A> aVariableDeclaration, PatternDSL.PatternDef<A> pattern) {
        this.aVariableDeclaration = aVariableDeclaration;
        this.aPattern = pattern;
    }


    public String getContextId() {
        return contextId;
    }

    public <A> Declaration<A> getAVariableDeclaration() {
        return (Declaration<A>) aVariableDeclaration;
    }

    public <A> PatternDSL.PatternDef<A> getAPattern() {
        return (PatternDSL.PatternDef<A>) aPattern;
    }

    public abstract T filter(Predicate predicate);

    public ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        return PatternDSL.on(scoreHolderGlobal, getAVariableDeclaration())
                .execute((drools, scoreHolder, __) -> {
                    RuleContext kcontext = (RuleContext) drools;
                    scoreHolder.impactScore(kcontext);
                });
    }

    public abstract ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToIntFunction matchWeighter);

    public abstract ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, ToLongFunction matchWeighter);

    public abstract ConsequenceBuilder._2<? extends AbstractScoreHolder, ?> prepareScoring(
            Global<? extends AbstractScoreHolder> scoreHolderGlobal, Function<Object, BigDecimal> matchWeighter);
}
