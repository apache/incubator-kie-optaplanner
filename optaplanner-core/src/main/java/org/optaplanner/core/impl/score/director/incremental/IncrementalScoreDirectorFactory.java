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

package org.optaplanner.core.impl.score.director.incremental;

import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

/**
 * Incremental implementation of {@link ScoreDirectorFactory}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see IncrementalScoreDirector
 * @see ScoreDirectorFactory
 */
public class IncrementalScoreDirectorFactory<Solution_> extends AbstractScoreDirectorFactory<Solution_> {

    private final Class<? extends IncrementalScoreCalculator> incrementalScoreCalculatorClass;
    private final Map<String, String> incrementalScoreCalculatorCustomProperties;

    public IncrementalScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            Class<? extends IncrementalScoreCalculator> incrementalScoreCalculatorClass,
            Map<String, String> incrementalScoreCalculatorCustomProperties) {
        super(solutionDescriptor);
        this.incrementalScoreCalculatorClass = incrementalScoreCalculatorClass;
        this.incrementalScoreCalculatorCustomProperties = incrementalScoreCalculatorCustomProperties;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public IncrementalScoreDirector<Solution_> buildScoreDirector(
            boolean lookUpEnabled, boolean constraintMatchEnabledPreference) {
        IncrementalScoreCalculator<Solution_> incrementalScoreCalculator = ConfigUtils.newInstance(this,
                "incrementalScoreCalculatorClass", incrementalScoreCalculatorClass);
        ConfigUtils.applyCustomProperties(incrementalScoreCalculator, "incrementalScoreCalculatorClass",
                incrementalScoreCalculatorCustomProperties, "incrementalScoreCalculatorCustomProperties");
        return new IncrementalScoreDirector<>(this,
                lookUpEnabled, constraintMatchEnabledPreference, incrementalScoreCalculator);
    }

}
