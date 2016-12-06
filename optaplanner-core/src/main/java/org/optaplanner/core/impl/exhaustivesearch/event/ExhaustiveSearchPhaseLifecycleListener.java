/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.exhaustivesearch.event;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.exhaustivesearch.scope.ExhaustiveSearchPhaseScope;
import org.optaplanner.core.impl.exhaustivesearch.scope.ExhaustiveSearchStepScope;
import org.optaplanner.core.impl.solver.event.SolverLifecycleListener;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the score type
 */
public interface ExhaustiveSearchPhaseLifecycleListener<Solution_, Score_ extends Score<Score_>>
        extends SolverLifecycleListener<Solution_> {

    void phaseStarted(ExhaustiveSearchPhaseScope<Solution_, Score_> phaseScope);

    void stepStarted(ExhaustiveSearchStepScope<Solution_, Score_> stepScope);

    void stepEnded(ExhaustiveSearchStepScope<Solution_, Score_> stepScope);

    void phaseEnded(ExhaustiveSearchPhaseScope<Solution_, Score_> phaseScope);

}
