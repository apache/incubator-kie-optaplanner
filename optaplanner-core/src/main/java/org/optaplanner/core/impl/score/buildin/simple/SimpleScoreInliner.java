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

package org.optaplanner.core.impl.score.buildin.simple;

import java.util.List;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.inliner.IntWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class SimpleScoreInliner extends ScoreInliner<SimpleScore> {

    private int score;

    protected SimpleScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, SimpleScore.ZERO);
    }

    @Override
    public IntWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            SimpleScore constraintWeight) {
        ensureNonZeroConstraintWeight(constraintWeight);
        int simpleConstraintWeight = constraintWeight.getScore();
        return (int matchWeight, Supplier<List<Object>> justifications) -> {
            int impact = simpleConstraintWeight * matchWeight;
            this.score += impact;
            return buildUndo(constraintPackage, constraintName, constraintWeight,
                    () -> this.score -= impact,
                    () -> SimpleScore.of(impact),
                    justifications);
        };
    }

    @Override
    public SimpleScore extractScore(int initScore) {
        return SimpleScore.ofUninitialized(initScore, score);
    }

    @Override
    public String toString() {
        return SimpleScore.class.getSimpleName() + " inliner";
    }

}
