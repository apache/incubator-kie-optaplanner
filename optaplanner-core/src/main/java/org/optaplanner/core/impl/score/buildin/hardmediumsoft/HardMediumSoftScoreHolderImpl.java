/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.buildin.hardmediumsoft;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScoreHolder;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;

/**
 * @see HardMediumSoftScore
 */
public final class HardMediumSoftScoreHolderImpl extends AbstractScoreHolder<HardMediumSoftScore>
        implements HardMediumSoftScoreHolder {

    protected final Map<Rule, IntMatchExecutor> matchExecutorByNumberMap = new LinkedHashMap<>();
    /** Slower than {@link #matchExecutorByNumberMap} */
    protected final Map<Rule, ScoreMatchExecutor<HardMediumSoftScore>> matchExecutorByScoreMap = new LinkedHashMap<>();

    protected int hardScore;
    protected int mediumScore;
    protected int softScore;

    public HardMediumSoftScoreHolderImpl(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardMediumSoftScore.ZERO);
    }

    public int getHardScore() {
        return hardScore;
    }

    public int getMediumScore() {
        return mediumScore;
    }

    public int getSoftScore() {
        return softScore;
    }

    // ************************************************************************
    // Setup methods
    // ************************************************************************

    @Override
    public void configureConstraintWeight(Rule rule, HardMediumSoftScore constraintWeight) {
        super.configureConstraintWeight(rule, constraintWeight);
        IntMatchExecutor matchExecutor;
        if (constraintWeight.equals(HardMediumSoftScore.ZERO)) {
            matchExecutor = (RuleContext kcontext, int weightMultiplier, Object... justifications) -> {
            };
        } else if (constraintWeight.getMediumScore() == 0 && constraintWeight.getSoftScore() == 0) {
            matchExecutor =
                    (RuleContext kcontext, int weightMultiplier, Object... justifications) -> addHardConstraintMatch(kcontext,
                            constraintWeight.getHardScore() * weightMultiplier, justifications);
        } else if (constraintWeight.getHardScore() == 0 && constraintWeight.getSoftScore() == 0) {
            matchExecutor =
                    (RuleContext kcontext, int weightMultiplier, Object... justifications) -> addMediumConstraintMatch(kcontext,
                            constraintWeight.getMediumScore() * weightMultiplier, justifications);
        } else if (constraintWeight.getHardScore() == 0 && constraintWeight.getMediumScore() == 0) {
            matchExecutor =
                    (RuleContext kcontext, int weightMultiplier, Object... justifications) -> addSoftConstraintMatch(kcontext,
                            constraintWeight.getSoftScore() * weightMultiplier, justifications);
        } else {
            matchExecutor =
                    (RuleContext kcontext, int weightMultiplier, Object... justifications) -> addMultiConstraintMatch(kcontext,
                            constraintWeight.getHardScore() * weightMultiplier,
                            constraintWeight.getMediumScore() * weightMultiplier,
                            constraintWeight.getSoftScore() * weightMultiplier, justifications);
        }
        matchExecutorByNumberMap.put(rule, matchExecutor);
        matchExecutorByScoreMap.put(rule, (RuleContext kcontext,
                HardMediumSoftScore weightMultiplier) -> addMultiConstraintMatch(kcontext,
                        constraintWeight.getHardScore() * weightMultiplier.getHardScore(),
                        constraintWeight.getMediumScore() * weightMultiplier.getMediumScore(),
                        constraintWeight.getSoftScore() * weightMultiplier.getSoftScore()));
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
    public void penalize(RuleContext kcontext, int hardWeightMultiplier, int mediumWeightMultiplier, int softWeightMultiplier) {
        impactScore(kcontext, -hardWeightMultiplier, -mediumWeightMultiplier, -softWeightMultiplier);
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
    public void reward(RuleContext kcontext, int hardWeightMultiplier, int mediumWeightMultiplier, int softWeightMultiplier) {
        impactScore(kcontext, hardWeightMultiplier, mediumWeightMultiplier, softWeightMultiplier);
    }

    @Override
    public void impactScore(RuleContext kcontext, Object... justifications) {
        impactScore(kcontext, 1, justifications);
    }

    @Override
    public void impactScore(RuleContext kcontext, int weightMultiplier, Object... justifications) {
        Rule rule = kcontext.getRule();
        IntMatchExecutor matchExecutor = matchExecutorByNumberMap.get(rule);
        if (matchExecutor == null) {
            throw new IllegalStateException("The DRL rule (" + rule.getPackageName() + ":" + rule.getName()
                    + ") does not match a @" + ConstraintWeight.class.getSimpleName() + " on the @"
                    + ConstraintConfiguration.class.getSimpleName() + " annotated class.");
        }
        matchExecutor.accept(kcontext, weightMultiplier, justifications);
    }

    @Override
    public void impactScore(RuleContext kcontext, long weightMultiplier, Object... justifications) {
        throw new UnsupportedOperationException("In the rule (" + kcontext.getRule().getName()
                + "), the scoreHolder class (" + getClass()
                + ") does not support a long weightMultiplier (" + weightMultiplier + ").\n"
                + "If you're using constraint streams, maybe switch from penalizeLong() to penalize().");
    }

    @Override
    public void impactScore(RuleContext kcontext, BigDecimal weightMultiplier, Object... justifications) {
        throw new UnsupportedOperationException("In the rule (" + kcontext.getRule().getName()
                + "), the scoreHolder class (" + getClass()
                + ") does not support a BigDecimal weightMultiplier (" + weightMultiplier + ").\n"
                + "If you're using constraint streams, maybe switch from penalizeBigDecimal() to penalize().");
    }

    private void impactScore(RuleContext kcontext, int hardWeightMultiplier, int mediumWeightMultiplier,
            int softWeightMultiplier) {
        Rule rule = kcontext.getRule();
        ScoreMatchExecutor<HardMediumSoftScore> matchExecutor = matchExecutorByScoreMap.get(rule);
        if (matchExecutor == null) {
            throw new IllegalStateException("The DRL rule (" + rule.getPackageName() + ":" + rule.getName()
                    + ") does not match a @" + ConstraintWeight.class.getSimpleName() + " on the @"
                    + ConstraintConfiguration.class.getSimpleName() + " annotated class.");
        }
        matchExecutor.accept(kcontext,
                HardMediumSoftScore.of(hardWeightMultiplier, mediumWeightMultiplier, softWeightMultiplier));
    }

    // ************************************************************************
    // Other match methods
    // ************************************************************************

    @Override
    public void addHardConstraintMatch(RuleContext kcontext, int hardWeight) {
        addHardConstraintMatch(kcontext, hardWeight, EMPTY_OBJECT_ARRAY);
    }

    private void addHardConstraintMatch(RuleContext kcontext, int hardWeight, Object... justifications) {
        hardScore += hardWeight;
        registerConstraintMatch(kcontext, () -> hardScore -= hardWeight, () -> HardMediumSoftScore.ofHard(hardWeight),
                justifications);
    }

    @Override
    public void addMediumConstraintMatch(RuleContext kcontext, int mediumWeight) {
        addMediumConstraintMatch(kcontext, mediumWeight, EMPTY_OBJECT_ARRAY);
    }

    private void addMediumConstraintMatch(RuleContext kcontext, int mediumWeight, Object... justifications) {
        mediumScore += mediumWeight;
        registerConstraintMatch(kcontext, () -> mediumScore -= mediumWeight, () -> HardMediumSoftScore.ofMedium(mediumWeight),
                justifications);
    }

    @Override
    public void addSoftConstraintMatch(RuleContext kcontext, int softWeight) {
        addSoftConstraintMatch(kcontext, softWeight, EMPTY_OBJECT_ARRAY);
    }

    private void addSoftConstraintMatch(RuleContext kcontext, int softWeight, Object... justifications) {
        softScore += softWeight;
        registerConstraintMatch(kcontext, () -> softScore -= softWeight, () -> HardMediumSoftScore.ofSoft(softWeight),
                justifications);
    }

    @Override
    public void addMultiConstraintMatch(RuleContext kcontext, int hardWeight, int mediumWeight, int softWeight) {
        addMultiConstraintMatch(kcontext, hardWeight, mediumWeight, softWeight, EMPTY_OBJECT_ARRAY);
    }

    private void addMultiConstraintMatch(RuleContext kcontext, int hardWeight, int mediumWeight, int softWeight,
            Object... justifications) {
        hardScore += hardWeight;
        mediumScore += mediumWeight;
        softScore += softWeight;
        registerConstraintMatch(kcontext,
                () -> {
                    hardScore -= hardWeight;
                    mediumScore -= mediumWeight;
                    softScore -= softWeight;
                },
                () -> HardMediumSoftScore.of(hardWeight, mediumWeight, softWeight),
                justifications);
    }

    @Override
    public HardMediumSoftScore extractScore(int initScore) {
        return HardMediumSoftScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

}
