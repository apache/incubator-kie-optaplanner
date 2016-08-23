/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.director.easy;

import java.util.Collection;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.director.AbstractScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Easy java implementation of {@link ScoreDirector}, which recalculates the {@link Score}
 * of the {@link PlanningSolution working solution} every time. This is non-incremental calculation, which is slow.
 * This score director implementation does not support {@link ScoreDirector#getConstraintMatchTotals()}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see ScoreDirector
 */
public class EasyScoreDirector<Solution_>
        extends AbstractScoreDirector<Solution_, EasyScoreDirectorFactory<Solution_>> {

    private final EasyScoreCalculator<Solution_> easyScoreCalculator;

    public EasyScoreDirector(EasyScoreDirectorFactory<Solution_> scoreDirectorFactory,
                             boolean constraintMatchEnabledPreference,
                             EasyScoreCalculator<Solution_> easyScoreCalculator) {
        super(scoreDirectorFactory, constraintMatchEnabledPreference);
        this.easyScoreCalculator = easyScoreCalculator;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public Score calculateScore() {
        variableListenerSupport.assertNotificationQueuesAreEmpty();
        Score score = easyScoreCalculator.calculateScore(workingSolution, workingInitScore);
        setCalculatedScore(score);
        return score;
    }

    /**
     * Always false, {@link ConstraintMatchTotal}s are not supported by this {@link ScoreDirector} implementation.
     * @return false
     */
    @Override
    public boolean isConstraintMatchEnabled() {
        return false;
    }

    /**
     * {@link ConstraintMatchTotal}s are not supported by this {@link ScoreDirector} implementation.
     * @throws IllegalStateException always
     * @return
     */
    @Override
    public Collection<ConstraintMatchTotal> getConstraintMatchTotals() {
        throw new IllegalStateException("ConstraintMatchTotals are not supported by EasyScoreDirector.");
    }

}
