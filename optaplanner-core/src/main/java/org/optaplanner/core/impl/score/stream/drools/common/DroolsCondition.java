/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.PatternDSL;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniCondition;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;

import static org.drools.model.DSL.accFunction;
import static org.drools.model.DSL.declarationOf;

/**
 * Encapsulates the low-level rule creation and manipulation operations via the Drools executable model DSL
 * (see {@link PatternDSL}.
 *
 * @param <T> type of Drools rule that we operate on
 */
public abstract class DroolsCondition<T extends DroolsRuleStructure> {

    protected final T ruleStructure;

    protected DroolsCondition(T ruleStructure) {
        this.ruleStructure = ruleStructure;
    }

    public T getRuleStructure() {
        return ruleStructure;
    }

    protected <S extends Score<S>, H extends AbstractScoreHolder<S>> void impactScore(Drools drools, H scoreHolder) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext);
    }

    protected <S extends Score<S>, H extends AbstractScoreHolder<S>> void impactScore(Drools drools, H scoreHolder,
            int impact) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext, impact);
    }

    protected <S extends Score<S>, H extends AbstractScoreHolder<S>> void impactScore(Drools drools, H scoreHolder,
            long impact) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext, impact);
    }

    protected <S extends Score<S>, H extends AbstractScoreHolder<S>> void impactScore(Drools drools, H scoreHolder,
            BigDecimal impact) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext, impact);
    }

    protected ViewItem<?> getInnerAccumulatePattern(PatternDef<Object> mainAccumulatePattern) {
        ViewItem[] items = Stream.concat(ruleStructure.getOpenRuleItems().stream(), Stream.of(mainAccumulatePattern))
                .toArray(ViewItem[]::new);
        return PatternDSL.and(items[0], Arrays.copyOfRange(items, 1, items.length));
    }

    protected <NewA, CarrierTuple, OutTuple, __> DroolsUniCondition<NewA> andCollect(
            DroolsAbstractAccumulateFunctionBridge<__, CarrierTuple, OutTuple> accumulateFunctionBridge,
            TriFunction<PatternDef<Object>, T, Variable<CarrierTuple>, PatternDef<Object>> bindFunction) {
        Variable<CarrierTuple> tupleVariable = ruleStructure.createVariable("tuple");
        PatternDef<Object> mainAccumulatePattern = ruleStructure.getPrimaryPattern()
                .expand(p -> bindFunction.apply(p, ruleStructure, tupleVariable))
                .build();
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(mainAccumulatePattern);
        Variable<NewA> outputVariable = (Variable<NewA>) declarationOf(Object.class, "collected");
        ViewItem<?> outerAccumulatePattern = DSL.accumulate(innerAccumulatePattern,
                accFunction(() -> accumulateFunctionBridge, tupleVariable).as(outputVariable));
        DroolsUniRuleStructure<NewA> newRuleStructure = ruleStructure.recollect(outputVariable, outerAccumulatePattern);
        return new DroolsUniCondition<>(newRuleStructure);
    }


}
