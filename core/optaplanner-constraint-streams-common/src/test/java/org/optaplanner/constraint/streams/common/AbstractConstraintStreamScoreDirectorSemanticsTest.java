/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorSemanticsTest;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactoryFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.constraintconfiguration.TestdataConstraintWeightConstraintProvider;

public abstract class AbstractConstraintStreamScoreDirectorSemanticsTest extends AbstractScoreDirectorSemanticsTest {

    protected abstract ConstraintStreamImplType getConstraintStreamImplType();

    @Override
    protected <Solution_> InnerScoreDirectorFactory<Solution_, SimpleScore>
            buildInnerScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor) {
        return buildInnerScoreDirectorFactory(solutionDescriptor, TestdataConstraintWeightConstraintProvider.class);
    }

    protected <Solution_> InnerScoreDirectorFactory<Solution_, SimpleScore>
            buildInnerScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
                    Class<? extends ConstraintProvider> constraintProviderClass) {
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig()
                .withConstraintProviderClass(constraintProviderClass)
                .withConstraintStreamImplType(getConstraintStreamImplType());
        ScoreDirectorFactoryFactory<Solution_, SimpleScore> scoreDirectorFactoryFactory =
                new ScoreDirectorFactoryFactory<>(scoreDirectorFactoryConfig);
        return scoreDirectorFactoryFactory.buildScoreDirectorFactory(getClass().getClassLoader(),
                EnvironmentMode.REPRODUCIBLE, solutionDescriptor);
    }

    @Test
    void polymorphismNullityFilter() {
        SolutionDescriptor<TestdataSolution> solutionDescriptor =
                TestdataSolution.buildSolutionDescriptor();
        InnerScoreDirectorFactory<TestdataSolution, SimpleScore> scoreDirectorFactory =
                buildInnerScoreDirectorFactory(solutionDescriptor, NullityFilterConstraintProvider.class);

        TestdataSolution solution = TestdataSolution.generateSolution(1, 2);
        TestdataEntity uninitializedEntity = solution.getEntityList().get(0);
        uninitializedEntity.setValue(null);
        try (InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector =
                scoreDirectorFactory.buildScoreDirector(false, true)) {
            // One of the entities has a null variable, it will therefore be uninitialized; standard forEach() behavior.
            scoreDirector.setWorkingSolution(solution);
            Assertions.assertThat(scoreDirector.calculateScore())
                    .isEqualTo(SimpleScore.ofUninitialized(-1, -2)); // One value, one initialized entity.

            scoreDirector.beforeVariableChanged(uninitializedEntity, "value");
            uninitializedEntity.setValue(new TestdataValue());
            scoreDirector.afterVariableChanged(uninitializedEntity, "value");

            // The entity is no longer null, and it will no longer be skipped.
            scoreDirector.triggerVariableListeners();
            Assertions.assertThat(scoreDirector.calculateScore())
                    .isEqualTo(SimpleScore.of(-3)); // One value, two initialized entities.
        }
    }

    public static final class NullityFilterConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    constraintFactory.forEach(Object.class)
                            .penalize("Penalize any entity with null variable", SimpleScore.ONE)
            };
        }

    }

}
