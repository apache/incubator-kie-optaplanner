/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.drl.holder;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScoreHolder;

/**
 * @see SimpleScore
 */
public final class SimpleScoreHolderImpl extends AbstractScoreHolder<SimpleScore> implements SimpleScoreHolder {

    protected final Map<Rule, IntMatchExecutor> matchExecutorByNumberMap = new LinkedHashMap<>();

    protected int score;

    public SimpleScoreHolderImpl(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    public int getScore() {
        return score;
    }

    // ************************************************************************
    // Setup methods
    // ************************************************************************

    @Override
    public void configureConstraintWeight(Rule rule, SimpleScore constraintWeight) {
        super.configureConstraintWeight(rule, constraintWeight);
        IntMatchExecutor matchExecutor;
        if (constraintWeight.isZero()) {
            matchExecutor = (RuleContext kcontext, int matchWeight) -> {
            };
        } else {
            matchExecutor = (RuleContext kcontext, int matchWeight) -> addConstraintMatch(kcontext,
                    constraintWeight.score() * matchWeight);
        }
        matchExecutorByNumberMap.put(rule, matchExecutor);
    }

    // ************************************************************************
    // Penalize and reward methods
    // ************************************************************************

    @Override
    public void penalize(RuleContext kcontext) {
        impactScore(kcontext, -1);
    }

    @Override
    public void penalize(RuleContext kcontext, int weightMultiplier) {
        impactScore(kcontext, -weightMultiplier);
    }

    @Override
    public void reward(RuleContext kcontext) {
        impactScore(kcontext, 1);
    }

    @Override
    public void reward(RuleContext kcontext, int weightMultiplier) {
        impactScore(kcontext, weightMultiplier);
    }

    @Override
    public void impactScore(RuleContext kcontext) {
        impactScore(kcontext, 1);
    }

    @Override
    public void impactScore(RuleContext kcontext, int weightMultiplier) {
        Rule rule = kcontext.getRule();
        IntMatchExecutor matchExecutor = matchExecutorByNumberMap.get(rule);
        if (matchExecutor == null) {
            throw new IllegalStateException("The DRL rule (" + rule.getPackageName() + ":" + rule.getName()
                    + ") does not match a @" + ConstraintWeight.class.getSimpleName() + " on the @"
                    + ConstraintConfiguration.class.getSimpleName() + " annotated class.");
        }
        matchExecutor.accept(kcontext, weightMultiplier);
    }

    @Override
    public void impactScore(RuleContext kcontext, long weightMultiplier) {
        throw new UnsupportedOperationException("In the rule (" + kcontext.getRule().getName()
                + "), the scoreHolder class (" + getClass()
                + ") does not support a long weightMultiplier (" + weightMultiplier + ").\n"
                + "If you're using constraint streams, maybe switch from penalizeLong() to penalize().");
    }

    @Override
    public void impactScore(RuleContext kcontext, BigDecimal weightMultiplier) {
        throw new UnsupportedOperationException("In the rule (" + kcontext.getRule().getName()
                + "), the scoreHolder class (" + getClass()
                + ") does not support a BigDecimal weightMultiplier (" + weightMultiplier + ").\n"
                + "If you're using constraint streams, maybe switch from penalizeBigDecimal() to penalize().");
    }

    // ************************************************************************
    // Other match methods
    // ************************************************************************

    @Override
    public void addConstraintMatch(RuleContext kcontext, int weight) {
        score += weight;
        registerConstraintMatch(kcontext, () -> score -= weight, () -> SimpleScore.of(weight));
    }

    @Override
    public SimpleScore extractScore(int initScore) {
        return SimpleScore.ofUninitialized(initScore, score);
    }

}
