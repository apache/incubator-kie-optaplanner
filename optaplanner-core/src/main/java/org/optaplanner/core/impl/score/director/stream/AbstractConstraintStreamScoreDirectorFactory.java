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

import java.util.Arrays;
import java.util.Objects;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.core.impl.score.stream.InnerConstraintFactory;

/**
 * FP streams implementation of {@link ScoreDirectorFactory}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the score type to go with the solution
 * @see ScoreDirectorFactory
 */
public abstract class AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_ extends Score<Score_>>
        extends AbstractScoreDirectorFactory<Solution_, Score_> {

    protected AbstractConstraintStreamScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor) {
        super(solutionDescriptor);
    }

    protected Constraint[] buildConstraints(ConstraintProvider constraintProvider,
            InnerConstraintFactory<Solution_> constraintFactory) {
        Constraint[] constraints = constraintProvider.defineConstraints(constraintFactory);
        if (constraints == null) {
            throw new IllegalStateException("The constraintProvider class (" + constraintProvider.getClass()
                    + ")'s defineConstraints() must not return null.\n"
                    + "Maybe return an empty array instead if there are no constraints.");
        }
        if (Arrays.stream(constraints).anyMatch(Objects::isNull)) {
            throw new IllegalStateException("The constraintProvider class (" + constraintProvider.getClass()
                    + ")'s defineConstraints() must not contain an element that is null.\n"
                    + "Maybe don't include any null elements in the " + Constraint.class.getSimpleName() + " array.");
        }
        return constraints;
    }

    public abstract Constraint[] getConstraints();

}
