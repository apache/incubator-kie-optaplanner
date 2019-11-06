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

package org.optaplanner.core.impl.score.stream.drools.quad;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block6;
import org.drools.model.functions.Predicate4;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsInferredMetadata;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsMetadata;

import static org.drools.model.DSL.on;

public final class DroolsQuadCondition<A, B, C, D> {

    private final DroolsMetadata<Object, A> aMetadata;
    private final DroolsMetadata<Object, B> bMetadata;
    private final DroolsMetadata<Object, C> cMetadata;
    private final DroolsMetadata<Object, D> dMetadata;

    public DroolsQuadCondition(DroolsMetadata<Object, A> aMetadata, DroolsMetadata<Object, B> bMetadata,
            DroolsMetadata<Object, C> cMetadata, DroolsMetadata<Object, D> dMetadata) {
        this.aMetadata = aMetadata;
        this.bMetadata = bMetadata;
        this.cMetadata = cMetadata;
        this.dMetadata = dMetadata;
    }

    public DroolsMetadata<Object, A> getAMetadata() {
        return aMetadata;
    }

    public DroolsMetadata<Object, B> getBMetadata() {
        return bMetadata;
    }

    public DroolsMetadata<Object, C> getCMetadata() {
        return cMetadata;
    }

    public DroolsMetadata<Object, D> getDMetadata() {
        return dMetadata;
    }

    public DroolsQuadCondition<A, B, C, D> andFilter(QuadPredicate<A, B, C, D> predicate) {
        Predicate4<Object, Object, Object, Object> filter = (d, a, b, c) -> predicate.test(aMetadata.extract(a),
                bMetadata.extract(b), cMetadata.extract(c), dMetadata.extract(d));
        Supplier<PatternDSL.PatternDef<Object>> patternSupplier = () -> dMetadata.buildPattern()
                .expr("Filter using " + predicate, aMetadata.getVariableDeclaration(),
                        bMetadata.getVariableDeclaration(), cMetadata.getVariableDeclaration(), filter);
        return new DroolsQuadCondition<>(aMetadata, bMetadata, cMetadata, dMetadata.substitute(patternSupplier));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, __, ___, ____, _____) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext);
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToIntQuadFunction<A, B, C, D> matchWeighter) {
        ToIntQuadFunction<Object, Object, Object, Object> weightMultiplier = (a, b, c, d) -> matchWeighter.applyAsInt(
                aMetadata.extract(a), bMetadata.extract(b), cMetadata.extract(c), dMetadata.extract(d));
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c, d) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier.applyAsInt(a, b, c, d));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToLongQuadFunction<A, B, C, D> matchWeighter) {
        ToLongQuadFunction<Object, Object, Object, Object> weightMultiplier = (a, b, c, d) -> matchWeighter.applyAsLong(
                aMetadata.extract(a), bMetadata.extract(b), cMetadata.extract(c), dMetadata.extract(d));
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c, d) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier.applyAsLong(a, b, c, d));
        });
    }

    public List<RuleItemBuilder<?>> completeWithScoring(
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        QuadFunction<Object, Object, Object, Object, BigDecimal> weightMultiplier = (a, b, c, d) -> matchWeighter.apply(
                aMetadata.extract(a), bMetadata.extract(b), cMetadata.extract(c), dMetadata.extract(d));
        return completeWithScoring(scoreHolderGlobal, (drools, scoreHolder, a, b, c, d) -> {
            RuleContext kcontext = (RuleContext) drools;
            scoreHolder.impactScore(kcontext, weightMultiplier.apply(a, b, c, d));
        });
    }

    private <ScoreHolder extends AbstractScoreHolder<?>> List<RuleItemBuilder<?>> completeWithScoring(
            Global<ScoreHolder> scoreHolderGlobal,
            Block6<Drools, ScoreHolder, Object, Object, Object, Object> consequenceImpl) {
        ConsequenceBuilder._5<ScoreHolder, Object, Object, Object, Object> consequence =
                on(scoreHolderGlobal, aMetadata.getVariableDeclaration(), bMetadata.getVariableDeclaration(),
                        cMetadata.getVariableDeclaration(), dMetadata.getVariableDeclaration())
                        .execute(consequenceImpl);
        if (aMetadata instanceof DroolsInferredMetadata && bMetadata instanceof DroolsInferredMetadata &&
                cMetadata instanceof DroolsInferredMetadata) {
            // In case of logical tuples, all patterns will be the same logical tuple, and therefore we just add one.
            return Arrays.asList(dMetadata.buildPattern(), consequence);
        } else {
            return Arrays.asList(aMetadata.buildPattern(), bMetadata.buildPattern(), cMetadata.buildPattern(),
                    dMetadata.buildPattern(), consequence);
        }
    }

}
