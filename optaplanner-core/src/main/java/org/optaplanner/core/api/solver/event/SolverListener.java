/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.solver.event;

import java.util.EventListener;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@FunctionalInterface
public interface SolverListener<Solution_> extends EventListener {

    /**
     * Called when the solver starts, including after every {@link Solver#addProblemFactChange(ProblemFactChange) restart}.
     * <p>
     * Called from the solver thread.
     * <b>Should return fast, because it steals time from the {@link Solver}.</b>
     * @param event never null
     */
    default void solvingStarted(SolvingStartedEvent<Solution_> event) {
    }

    /**
     * Called when the solver ends, including before every {@link Solver#addProblemFactChange(ProblemFactChange) restart}.
     * <p>
     * Called from the solver thread.
     * <b>Should return fast, because it steals time from the {@link Solver}.</b>
     * @param event never null
     */
    void solvingEnded(SolvingEndedEvent<Solution_> event);

}
