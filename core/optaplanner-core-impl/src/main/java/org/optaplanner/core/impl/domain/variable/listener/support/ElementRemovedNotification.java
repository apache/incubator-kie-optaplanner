package org.optaplanner.core.impl.domain.variable.listener.support;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;

final class ElementRemovedNotification<Solution_> implements ListVariableNotification<Solution_> {

    private final Object element;

    ElementRemovedNotification(Object element) {
        this.element = element;
    }

    @Override
    public void triggerBefore(ListVariableListener<Solution_, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        throw new UnsupportedOperationException("ListVariableListeners do not listen for this event.");
    }

    @Override
    public void triggerAfter(ListVariableListener<Solution_, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        variableListener.afterListVariableElementRemoved(scoreDirector, element);
    }

    @Override
    public String toString() {
        return "ElementRemoved(" + element + ")";
    }
}
