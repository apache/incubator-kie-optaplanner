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

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.director.AbstractScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Collection;

/**
 * Easy java implementation of {@link ScoreDirector}, which recalculates the {@link Score}
 * of the {@link Solution_} workingSolution every time. This is non-incremental calculation, which is slow.
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

    public Score calculateScore() {
        variableListenerSupport.assertNotificationQueuesAreEmpty();
        Score score = easyScoreCalculator.calculateScore(workingSolution);
        setCalculatedScore(score);
        return score;
    }

    public boolean isConstraintMatchEnabled() {
        return false;
    }

    public Collection<ConstraintMatchTotal> getConstraintMatchTotals() {
        throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                + ") is disabled, this method should not be called.");
    }

}
