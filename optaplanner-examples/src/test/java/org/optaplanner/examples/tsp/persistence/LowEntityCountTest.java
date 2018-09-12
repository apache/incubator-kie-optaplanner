package org.optaplanner.examples.tsp.persistence;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.examples.tsp.app.TspApp;
import org.optaplanner.examples.tsp.domain.TspSolution;

public class LowEntityCountTest {

    public static final long LOW_ENTITY_SECONDS_TERMINATION = 1;
    public static final String PATH_TO_NEARBY_SELECTION_XML = "org/optaplanner/examples/tsp/lowentitycount/nearbySelection.xml";
    private TspApp app = new TspApp();

    @Test
    public void zeroEntity() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.getUnsolvedProblem("oneDomicileOnly.xml");
        Assertions.assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");

    }

    @Test
    public void oneEntity() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.getUnsolvedProblem("oneDomicileOneEntity.xml");
        tspSolutionSolver.solve(problem);
    }

    @Test
    public void twoEntities() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION);
        TspSolution problem = app.getUnsolvedProblem("twoEnt.xml");
        tspSolutionSolver.solve(problem);
    }

    @Test
    public void zeroEntityWithNearbySelection() {
        TspSolution problem = app.getUnsolvedProblem("oneDomicileOnly.xml");
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                PATH_TO_NEARBY_SELECTION_XML);
        Assertions.assertThatIllegalStateException().isThrownBy(() -> tspSolutionSolver.solve(problem)).
                withMessageContaining("annotated member").withMessageContaining("must not return");
    }

    @Test
    public void oneEntityWithNearbySelection() {
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                PATH_TO_NEARBY_SELECTION_XML);
        TspSolution problem = app.getUnsolvedProblem("oneDomicileOneEntity.xml");
        tspSolutionSolver.solve(problem);
    }

    @Test
    public void twoEntitiesWithNearbySelection() {
        TspSolution problem = app.getUnsolvedProblem("twoEnt.xml");
        Solver<TspSolution> tspSolutionSolver = app.createSolver(LOW_ENTITY_SECONDS_TERMINATION,
                PATH_TO_NEARBY_SELECTION_XML);
        tspSolutionSolver.solve(problem);
    }
}
