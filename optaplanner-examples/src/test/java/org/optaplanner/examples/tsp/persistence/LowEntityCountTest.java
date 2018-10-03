/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.tsp.persistence;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.examples.tsp.app.TspApp;
import org.optaplanner.examples.tsp.domain.TspSolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class LowEntityCountTest {

    public static final long LOW_ENTITY_SECONDS_TERMINATION = 1;
    public static final String PATH_TO_NEARBY_SELECTION_XML = "org/optaplanner/examples/lowentitycount/nearbySelection.xml";
    private TspApp app = new TspApp();

    @Test
    public void zeroEntity() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.loadUnsolvedProblemFromResource("oneDomicileOnly.xml");
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Test
    public void oneEntity() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.loadUnsolvedProblemFromResource("oneDomicileOneEntity.xml");
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void twoEntities() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.loadUnsolvedProblemFromResource("twoEnt.xml");
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void zeroEntityWithNearbySelection() {
        TspSolution problem = app.loadUnsolvedProblemFromResource("oneDomicileOnly.xml");
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                 PATH_TO_NEARBY_SELECTION_XML);
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Test
    public void oneEntityWithNearbySelection() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                 PATH_TO_NEARBY_SELECTION_XML);
        TspSolution problem = app.loadUnsolvedProblemFromResource("oneDomicileOneEntity.xml");
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void twoEntitiesWithNearbySelection() {
        TspSolution problem = app.loadUnsolvedProblemFromResource("twoEnt.xml");
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                 PATH_TO_NEARBY_SELECTION_XML);
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }
}
