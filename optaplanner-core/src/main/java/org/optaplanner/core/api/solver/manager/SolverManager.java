/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.solver.manager;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.solver.manager.DefaultSolverManager;

public interface SolverManager<Solution_> {

    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource) {
        return new DefaultSolverManager<>(solverConfigResource);
    }

    static <Solution_> DefaultSolverManager<Solution_> createFromXmlResource(String solverConfigResource, ClassLoader classLoader) {
        return new DefaultSolverManager<>(solverConfigResource, classLoader);
    }

    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource, ThreadFactory threadFactory) {
        return new DefaultSolverManager<>(solverConfigResource, threadFactory);
    }

    static <Solution_> SolverManager<Solution_> createFromXmlResource(String solverConfigResource,
                                                                      ClassLoader classLoader, ThreadFactory threadFactory) {
        return new DefaultSolverManager<>(solverConfigResource, classLoader, threadFactory);
    }

    void solve(Object problemId,
               Solution_ planningProblem,
               Consumer<Solution_> onBestSolutionChangedEvent,
               Consumer<Solution_> onSolvingEnded);

    void solve(Object problemId,
               Solution_ planningProblem,
               Consumer<Solution_> onBestSolutionChangedEvent,
               Consumer<Solution_> onSolvingEnded,
               Consumer<Throwable> onException);

    void stopSolver(Object problemId);

    boolean isProblemSubmitted(Object problemId);

    Solution_ getBestSolution(Object problemId);

    Score<?> getBestScore(Object problemId);

    SolverStatus getSolverStatus(Object problemId);

    void shutdown();

}
