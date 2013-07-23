/*
 * Copyright 2010 JBoss Inc
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

package org.optaplanner.core.api.score.buildin.simple;

import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * @see SimpleScore
 */
public class SimpleScoreHolder extends AbstractScoreHolder {

    protected int score;

    public SimpleScoreHolder(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    public int getScore() {
        return score;
    }

    @Deprecated
    public void setScore(int score) {
        this.score = score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void addConstraintMatch(RuleContext kcontext, final int weight) {
        score += weight;
        registerIntConstraintMatch(kcontext, 0, weight, new Runnable() {
            public void run() {
                score -= weight;
            }
        });
    }

    public Score extractScore() {
        return SimpleScore.valueOf(score);
    }

}
