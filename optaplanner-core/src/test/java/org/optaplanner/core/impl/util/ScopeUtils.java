package org.optaplanner.core.impl.util;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public final class ScopeUtils {

    /**
     * Mocks {@link AbstractPhaseScope} that will delegate to {@link SolverScope#getWorkingRandom()}.
     * 
     * @param solverScope never null
     * @param <Solution_> generic type of the solution
     * @return never null
     */
    public static <Solution_> AbstractPhaseScope<Solution_> delegatingPhaseScope(SolverScope<Solution_> solverScope) {
        return new AbstractPhaseScope<>(solverScope) {
            @Override
            public AbstractStepScope<Solution_> getLastCompletedStepScope() {
                return null;
            }
        };
    }

    /**
     * Mocks {@link AbstractPhaseScope} that will delegate to {@link AbstractPhaseScope#getWorkingRandom()}.
     * 
     * @param phaseScope never null
     * @param <Solution_> generic type of the solution
     * @return never null
     */
    public static <Solution_> AbstractStepScope<Solution_> delegatingStepScope(AbstractPhaseScope<Solution_> phaseScope) {
        return new AbstractStepScope<>(0) {
            @Override
            public AbstractPhaseScope<Solution_> getPhaseScope() {
                return phaseScope;
            }
        };
    }

    private ScopeUtils() {
        // No external instances.
    }

}
