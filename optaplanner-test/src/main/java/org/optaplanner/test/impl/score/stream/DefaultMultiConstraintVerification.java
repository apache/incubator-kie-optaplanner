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

package org.optaplanner.test.impl.score.stream;

import java.util.Objects;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.stream.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.test.api.score.stream.MultiConstraintVerification;

public final class DefaultMultiConstraintVerification<Solution_, Score_ extends Score<Score_>>
        implements MultiConstraintVerification<Solution_> {

    private final AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory;
    private final ConstraintProvider constraintProvider;

    protected DefaultMultiConstraintVerification(
            AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory,
            ConstraintProvider constraintProvider) {
        this.scoreDirectorFactory = scoreDirectorFactory;
        this.constraintProvider = constraintProvider;
    }

    @Override
    public final DefaultMultiConstraintAssertion<Solution_, Score_> given(Object... facts) {
        InnerScoreDirector<Solution_, Score_> scoreDirector = scoreDirectorFactory.buildScoreDirector(true, true);
        SolutionDescriptor<Solution_> solutionDescriptor = scoreDirectorFactory.getSolutionDescriptor();
        for (Object fact : facts) {
            if (solutionDescriptor.hasEntityDescriptor(fact.getClass())) {
                scoreDirector.beforeEntityAdded(fact);
                scoreDirector.afterEntityAdded(fact);
            } else {
                scoreDirector.beforeProblemFactAdded(fact);
                scoreDirector.afterProblemFactAdded(fact);
            }
        }
        Score_ score = scoreDirector.calculateScore();
        return new DefaultMultiConstraintAssertion<>(constraintProvider, score.withInitScore(0),
                scoreDirector.getConstraintMatchTotalMap(), scoreDirector.getIndictmentMap());
    }

    @Override
    public final DefaultMultiConstraintAssertion<Solution_, Score_> givenSolution(Solution_ solution) {
        try (InnerScoreDirector<Solution_, Score_> scoreDirector =
                scoreDirectorFactory.buildScoreDirector(true, true)) {
            scoreDirector.setWorkingSolution(Objects.requireNonNull(solution));
            return new DefaultMultiConstraintAssertion<>(constraintProvider, scoreDirector.calculateScore(),
                    scoreDirector.getConstraintMatchTotalMap(), scoreDirector.getIndictmentMap());
        }
    }

}
