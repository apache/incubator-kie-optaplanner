package org.optaplanner.core.impl.domain.variable.listener;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public abstract class AbstractFactInsensitiveVariableListener<Solution_, Entity_>
        implements VariableListener<Solution_, Entity_> {

    @Override
    public void beforeProblemFactAdded(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // This listener is not fact-sensitive.
    }

    @Override
    public void afterProblemFactAdded(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // This listener is not fact-sensitive.
    }

    @Override
    public void beforeProblemPropertyChanged(ScoreDirector<Solution_> scoreDirector, Object problemFactOrEntity) {
        // This listener is not fact-sensitive.
    }

    @Override
    public void afterProblemPropertyChanged(ScoreDirector<Solution_> scoreDirector, Object problemFactOrEntity) {
        // This listener is not fact-sensitive.
    }

    @Override
    public void beforeProblemFactRemoved(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // This listener is not fact-sensitive.
    }

    @Override
    public void afterProblemFactRemoved(ScoreDirector<Solution_> scoreDirector, Object fact) {
        // This listener is not fact-sensitive.
    }

}
