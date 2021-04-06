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

package org.optaplanner.core.impl.score.director.stream;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintFactory;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintSession;

public final class BavetConstraintStreamScoreDirectorFactory<Solution_, Score_ extends Score<Score_>>
        extends AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> {

    public BavetConstraintStreamScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            ConstraintProvider constraintProvider) {
        super(solutionDescriptor, constraintProvider, () -> new BavetConstraintFactory<>(solutionDescriptor));
    }

    @Override
    public BavetConstraintStreamScoreDirector<Solution_, Score_> buildScoreDirector(boolean lookUpEnabled,
            boolean constraintMatchEnabledPreference) {
        return new BavetConstraintStreamScoreDirector<>(this, lookUpEnabled, constraintMatchEnabledPreference);
    }

    public BavetConstraintSession<Solution_, Score_> newSession(boolean constraintMatchEnabled,
            Solution_ workingSolution) {
        return (BavetConstraintSession<Solution_, Score_>) getConstraintSessionFactory().buildSession(constraintMatchEnabled,
                workingSolution);
    }

}
