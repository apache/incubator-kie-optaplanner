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

package org.optaplanner.core.api.solver;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.config.solver.SolverManagerConfig;

/**
 * The status of {@link PlanningSolution problem} submitted to the {@link SolverManager}.
 * Retrieve this status with {@link SolverManager#getSolverStatus(Object)} or {@link SolverJob#getSolverStatus()}.
 */
public enum SolverStatus {
    /**
     * No solver thread started solving this problem yet, but sooner or later a solver thread will solve it.
     * <p>
     * For example, submitting 7 problems to a {@link SolverManager}
     * with a {@link SolverManagerConfig#getParallelSolverCount()} of 4,
     * puts 3 into this state for non-trivial amount of time.
     * <p>
     * Transitions into {@link #SOLVING_ACTIVE} (or {@link #NOT_SOLVING} if it is
     * {@link SolverManager#terminateEarly(Object) terminated early}, before it starts).
     */
    SOLVING_SCHEDULED,
    /**
     * A solver thread started solving the problem, but hasn't finished yet.
     * <p>
     * If CPU resource are scarce and that solver thread is waiting for CPU time,
     * the state doesn't change, it's still considered solving active.
     * <p>
     * Transitions into {@link #NOT_SOLVING} when terminated.
     */
    SOLVING_ACTIVE,
    /**
     * The problem's solving has terminated or the problem was never submitted to the {@link SolverManager}.
     * {@link SolverManager#getSolverStatus(Object)} cannot tell the difference,
     * but {@link SolverJob#getSolverStatus()} can.
     */
    NOT_SOLVING;
}
