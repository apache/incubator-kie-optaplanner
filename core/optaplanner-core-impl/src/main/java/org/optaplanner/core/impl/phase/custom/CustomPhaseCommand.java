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

package org.optaplanner.core.impl.phase.custom;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.phase.Phase;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * Runs a custom algorithm as a {@link Phase} of the {@link Solver} that changes the planning variables.
 * Do not abuse to change the problems facts,
 * instead use {@link Solver#addProblemFactChange(ProblemFactChange)} for that.
 * <p>
 * To add custom properties, configure custom properties and add public setters for them.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@FunctionalInterface
public interface CustomPhaseCommand<Solution_> {

    /**
     * Changes {@link PlanningSolution working solution} of {@link ScoreDirector#getWorkingSolution()}.
     * When the {@link PlanningSolution working solution} is modified, the {@link ScoreDirector} must be correctly notified
     * (through {@link ScoreDirector#beforeVariableChanged(Object, String)} and
     * {@link ScoreDirector#afterVariableChanged(Object, String)}),
     * otherwise calculated {@link Score}s will be corrupted.
     * <p>
     * Don't forget to call {@link ScoreDirector#triggerVariableListeners()} after each set of changes
     * (especially before every {@link InnerScoreDirector#calculateScore()} call)
     * to ensure all shadow variables are updated.
     *
     * @param scoreDirector never null, the {@link ScoreDirector} that needs to get notified of the changes.
     */
    void changeWorkingSolution(ScoreDirector<Solution_> scoreDirector);

}
