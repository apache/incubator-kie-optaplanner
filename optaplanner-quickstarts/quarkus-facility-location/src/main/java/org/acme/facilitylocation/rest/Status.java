/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.facilitylocation.rest;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.optaplanner.core.api.solver.SolverStatus;

class Status {
    public final FacilityLocationProblem solution;
    public final boolean isSolving;

    Status(FacilityLocationProblem solution, SolverStatus solverStatus) {
        this.solution = solution;
        this.isSolving = solverStatus != SolverStatus.NOT_SOLVING;
    }
}
