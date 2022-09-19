package org.optaplanner.core.impl.domain.variable.listener.support;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;

final class SubListChangedNotification<Solution_> extends AbstractNotification implements ListVariableNotification<Solution_> {

    private final int toIndex;

    SubListChangedNotification(Object entity, int fromIndex, int toIndex) {
        super(entity, fromIndex);
        this.toIndex = toIndex;
    }

    @Override
    public void triggerBefore(ListVariableListener<Solution_, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        variableListener.beforeSubListChanged(scoreDirector, entity, index, toIndex);
    }

    @Override
    public void triggerAfter(ListVariableListener<Solution_, Object> variableListener, ScoreDirector<Solution_> scoreDirector) {
        variableListener.afterSubListChanged(scoreDirector, entity, index, toIndex);
    }

    @Override
    public String toString() {
        return "SubListChangedNotification(" + entity + "[" + index + ", " + toIndex + "])";
    }
}