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

package org.optaplanner.core.api.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;

import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.holder.ScoreHolder;

/**
 * @see HardSoftBigDecimalScore
 */
public interface HardSoftBigDecimalScoreHolder extends ScoreHolder<HardSoftBigDecimalScore> {

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void penalize(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the specific weightMultiplier per score
     * level.
     * Slower than {@link #penalize(RuleContext, BigDecimal)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightMultiplier at least 0
     * @param softWeightMultiplier at least 0
     */
    void penalize(RuleContext kcontext, BigDecimal hardWeightMultiplier, BigDecimal softWeightMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void reward(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the specific weightMultiplier per score level.
     * Slower than {@link #reward(RuleContext, BigDecimal)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightMultiplier at least 0
     * @param softWeightMultiplier at least 0
     */
    void reward(RuleContext kcontext, BigDecimal hardWeightMultiplier, BigDecimal softWeightMultiplier);

    void impactScore(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addHardConstraintMatch(RuleContext kcontext, BigDecimal hardWeight);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param softWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addSoftConstraintMatch(RuleContext kcontext, BigDecimal softWeight);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeight never null, higher is better, negative for a penalty, positive for a reward
     * @param softWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addMultiConstraintMatch(RuleContext kcontext, BigDecimal hardWeight, BigDecimal softWeight);
}
