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

package org.optaplanner.core.impl.solver.manager;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.api.solver.manager.SolverStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolverTask<Solution_> {

    private static final Logger logger = LoggerFactory.getLogger(SolverTask.class);

    private final Object problemId;
    private Solver<Solution_> solver;
    private Solution_ planningProblem;

    public SolverTask(Object problemId, Solver<Solution_> solver, Solution_ planningProblem) {
        this.problemId = problemId;
        this.solver = solver;
        this.planningProblem = planningProblem;
    }

    public Solution_ startSolving() {
        logger.info("Running solverTask for problemId ({}).", problemId);
        return solver.solve(planningProblem);
    }

    public Object getProblemId() {
        return problemId;
    }

    public synchronized Solution_ getBestSolution() {
        // TODO possible race condition: planningProblem might change by solver thread
        Solution_ bestSolution = solver.getBestSolution();
        return bestSolution == null ? planningProblem : bestSolution;
    }

    public Score<?> getBestScore() {
        return solver.getBestScore();
    }

    public synchronized SolverStatus getSolverStatus() {
        if (solver.isTerminateEarly()) {
            return SolverStatus.TERMINATED_EARLY;
        } else if (solver.isSolving()) {
            return SolverStatus.SOLVING;
        } else {
            return SolverStatus.STOPPED;
        }
    }

    public void addEventListener(SolverEventListener<Solution_> eventListener) {
        solver.addEventListener(eventListener);
    }

    public void stopSolver() {
        solver.terminateEarly();
    }
}
