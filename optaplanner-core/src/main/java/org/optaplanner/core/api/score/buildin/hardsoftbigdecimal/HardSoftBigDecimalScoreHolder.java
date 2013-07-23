/*
 * Copyright 2013 JBoss Inc
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
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * @see HardSoftBigDecimalScore
 */
public class HardSoftBigDecimalScoreHolder extends AbstractScoreHolder {

    protected BigDecimal hardScore;
    protected BigDecimal softScore;

    public HardSoftBigDecimalScoreHolder(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    public BigDecimal getHardScore() {
        return hardScore;
    }

    @Deprecated
    public void setHardScore(BigDecimal hardScore) {
        this.hardScore = hardScore;
    }

    public BigDecimal getSoftScore() {
        return softScore;
    }

    @Deprecated
    public void setSoftScore(BigDecimal softScore) {
        this.softScore = softScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void addHardConstraintMatch(RuleContext kcontext, final BigDecimal weight) {
        hardScore = hardScore.add(weight);
        registerBigDecimalConstraintMatch(kcontext, 0, weight, new Runnable() {
            public void run() {
                hardScore = hardScore.subtract(weight);
            }
        });
    }

    public void addSoftConstraintMatch(RuleContext kcontext, final BigDecimal weight) {
        softScore = softScore.add(weight);
        registerBigDecimalConstraintMatch(kcontext, 1, weight, new Runnable() {
            public void run() {
                softScore = softScore.subtract(weight);
            }
        });
    }

    public Score extractScore() {
        return HardSoftBigDecimalScore.valueOf(hardScore, softScore);
    }

}
