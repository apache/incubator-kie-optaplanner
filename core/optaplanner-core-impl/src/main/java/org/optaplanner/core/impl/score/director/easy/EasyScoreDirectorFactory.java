/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.score.director.easy;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

/**
 * Easy implementation of {@link ScoreDirectorFactory}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the score type to go with the solution
 * @see EasyScoreDirector
 * @see ScoreDirectorFactory
 */
public class EasyScoreDirectorFactory<Solution_, Score_ extends Score<Score_>>
        extends AbstractScoreDirectorFactory<Solution_, Score_> {

    private final EasyScoreCalculator<Solution_, Score_> easyScoreCalculator;

    public EasyScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            EasyScoreCalculator<Solution_, Score_> easyScoreCalculator) {
        super(solutionDescriptor);
        this.easyScoreCalculator = easyScoreCalculator;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public EasyScoreDirector<Solution_, Score_> buildScoreDirector(
            boolean lookUpEnabled, boolean constraintMatchEnabledPreference) {
        return new EasyScoreDirector<>(this, lookUpEnabled, constraintMatchEnabledPreference, easyScoreCalculator);
    }

}
