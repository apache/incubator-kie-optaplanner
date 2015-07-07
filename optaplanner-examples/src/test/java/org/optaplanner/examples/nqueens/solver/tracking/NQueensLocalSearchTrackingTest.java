package org.optaplanner.examples.nqueens.solver.tracking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.examples.nqueens.app.NQueensApp;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.persistence.NQueensGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class NQueensLocalSearchTrackingTest extends NQueensTrackingTest {

    private final AcceptorConfig acceptorConfig;
    private final List<NQueensStepTracking> expectedCoordinates;

    public NQueensLocalSearchTrackingTest(AcceptorConfig acceptorConfig,
                                          List<NQueensStepTracking> expectedCoordinates) {
        this.expectedCoordinates = expectedCoordinates;
        this.acceptorConfig = acceptorConfig;
    }

    @Test
    public void testConstructionHeuristics() {
        SolverConfig config = SolverFactory.createFromXmlResource(NQueensApp.SOLVER_CONFIG).getSolverConfig();

        int N = 6;
        NQueensGenerator generator = new NQueensGenerator();
        NQueens planningProblem = NQueensSolutionInitializer.initialize(generator.createNQueens(N));

        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        localSearchPhaseConfig.setAcceptorConfig(acceptorConfig);
        localSearchPhaseConfig.setForagerConfig(new LocalSearchForagerConfig());
        localSearchPhaseConfig.getForagerConfig().setAcceptedCountLimit(planningProblem.getN() * planningProblem.getN());
        localSearchPhaseConfig.getForagerConfig().setBreakTieRandomly(false);

        NQueensStepTracker listener = new NQueensStepTracker(NQueensSolutionInitializer.initialize(generator.createNQueens(N)));
        DefaultSolver solver = (DefaultSolver) config.buildSolver();
        solver.addPhaseLifecycleListener(listener);
        solver.solve(planningProblem);

        NQueens bestSolution = (NQueens) solver.getBestSolution();

        assertNotNull(bestSolution);
        assertTrackingList(expectedCoordinates, listener.getTrackingList());
    }

    @Parameterized.Parameters(name = "ConstructionHeuristicType: {0}, EntitySorterManner: {1}, ValueSorterManner: {2}")
    public static Collection<Object[]> parameters() {
        Collection<Object[]> params = new ArrayList<Object[]>();

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAcceptorTypeList(Arrays.asList(AcceptorType.HILL_CLIMBING));
        params.add(new Object[]{acceptorConfig, Arrays.asList(new NQueensStepTracking(1, 5),
                new NQueensStepTracking(5, 4), new NQueensStepTracking(0, 1),
                new NQueensStepTracking(3, 3), new NQueensStepTracking(4, 1),
                new NQueensStepTracking(2, 2), new NQueensStepTracking(1, 3),
                new NQueensStepTracking(5, 0), new NQueensStepTracking(0, 2),
                new NQueensStepTracking(3, 4), new NQueensStepTracking(4, 5),
                new NQueensStepTracking(2, 1), new NQueensStepTracking(1, 5),
                new NQueensStepTracking(4, 4), new NQueensStepTracking(5, 3),
                new NQueensStepTracking(4, 0))});
        
        return params;
    }


}
