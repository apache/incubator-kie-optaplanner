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

package org.optaplanner.core.impl.score;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class DefaultScoreManager<Solution_> implements ScoreManager<Solution_> {

    private ScoreDirectorFactory<Solution_> scoreDirectorFactory;

    public DefaultScoreManager(ScoreDirectorFactory<Solution_> scoreDirectorFactory) {
        this.scoreDirectorFactory = scoreDirectorFactory;
    }

    public ScoreDirectorFactory<Solution_> getScoreDirectorFactory() {
        return scoreDirectorFactory;
    }

    @Override
    public Score updateScore(Solution_ solution) {
        try (ScoreDirector<Solution_> scoreDirector = scoreDirectorFactory.buildScoreDirector()) {
            scoreDirector.setWorkingSolution(solution);
            return scoreDirector.calculateScore();
        }
    }

    @Override
    public String explainScore(Solution_ solution) {
        try (ScoreDirector<Solution_> scoreDirector = scoreDirectorFactory.buildScoreDirector()) {
            scoreDirector.setWorkingSolution(solution);
            return scoreDirector.explainScore();
        }
    }
}
