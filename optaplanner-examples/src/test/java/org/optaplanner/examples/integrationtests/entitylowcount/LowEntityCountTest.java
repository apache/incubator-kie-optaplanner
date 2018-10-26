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

package org.optaplanner.examples.integrationtests.entitylowcount;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.examples.common.app.DataSetLoader;
import org.optaplanner.examples.common.app.SolverBuilder;
import org.optaplanner.examples.tsp.app.TspApp;
import org.optaplanner.examples.tsp.domain.TspSolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class LowEntityCountTest {

    public static final long LOW_ENTITY_SECONDS_TERMINATION = 1;
    public static final String PATH_TO_NEARBY_SELECTION_XML = "org/optaplanner/examples/integrationtests/lowentitycount/data/nearbySelection.xml";
    public static final String PATH_TO_DATA_DIRECTORY = "/org/optaplanner/examples/integrationtests/lowentitycount/data/";
    private TspApp app = new TspApp();

    @Test
    public void zeroEntity() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%soneDomicileOnly.xml", PATH_TO_DATA_DIRECTORY));
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Test
    public void oneEntity() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%soneDomicileOneEntity.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void twoEntities() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION, app.getSolverConfig());
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%stwoEnt.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void zeroEntityWithNearbySelection() {
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%soneDomicileOnly.xml", PATH_TO_DATA_DIRECTORY));
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                           PATH_TO_NEARBY_SELECTION_XML);
        assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Test
    public void oneEntityWithNearbySelection() {
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                           PATH_TO_NEARBY_SELECTION_XML);
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%soneDomicileOneEntity.xml", PATH_TO_DATA_DIRECTORY));
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }

    @Test
    public void twoEntitiesWithNearbySelection() {
        TspSolution problem = DataSetLoader.loadUnsolvedProblemFromResource(app, String.format("%stwoEnt.xml", PATH_TO_DATA_DIRECTORY));
        Solver<TspSolution> tspSolutionSolver = SolverBuilder.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                                                                           PATH_TO_NEARBY_SELECTION_XML);
        TspSolution solvedProblem = tspSolutionSolver.solve(problem);
        assertThat(solvedProblem.getVisitList()).allMatch((visit) -> visit.getPreviousStandstill() != null);
    }
}
