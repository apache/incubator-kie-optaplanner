package org.optaplanner.core.impl.domain.variable.listener;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.change.ProblemChange;

/**
 * Used in situations where the variable listener does not read problem facts
 * or any information in entities other than planning variables.
 * This considerably improves performance of {@link ProblemChange}s.
 *
 * @param <Solution_>
 * @param <Entity_>
 */
public abstract class AbstractProblemPropertyInsensitiveVariableListener<Solution_, Entity_>
        implements VariableListener<Solution_, Entity_> {

    @Override
    public void beforeProblemFactAdded(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // Do nothing.
    }

    @Override
    public void afterProblemFactAdded(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // Do nothing.
    }

    @Override
    public void beforeProblemPropertyChanged(ScoreDirector<Solution_> scoreDirector, Object problemFactOrEntity) {
        // Do nothing.
    }

    @Override
    public void afterProblemPropertyChanged(ScoreDirector<Solution_> scoreDirector, Object problemFactOrEntity) {
        // Do nothing.
    }

    @Override
    public void beforeProblemFactRemoved(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // Do nothing.
    }

    @Override
    public void afterProblemFactRemoved(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // Do nothing.
    }

}
