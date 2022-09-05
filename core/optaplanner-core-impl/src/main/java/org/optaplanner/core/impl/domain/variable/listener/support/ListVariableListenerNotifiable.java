package org.optaplanner.core.impl.domain.variable.listener.support;

import java.util.Collection;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;

/**
 * A notifiable specialized to receive {@link ListVariableNotification}s and trigger them on a given
 * {@link ListVariableListener}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
final class ListVariableListenerNotifiable<Solution_>
        extends AbstractNotifiable<Solution_, ListVariableListener<Solution_, Object>> {

    ListVariableListenerNotifiable(
            ScoreDirector<Solution_> scoreDirector,
            ListVariableListener<Solution_, Object> variableListener,
            Collection<Notification<Solution_, ? super ListVariableListener<Solution_, Object>>> notificationQueue,
            int globalOrder) {
        super(scoreDirector, variableListener, notificationQueue, globalOrder);
    }

    public void notifyBefore(Notification<Solution_, ListVariableListener<Solution_, Object>> notification) {
        notification.triggerBefore(variableListener, scoreDirector);
    }

    public void notifyAfter(Notification<Solution_, ListVariableListener<Solution_, Object>> notification) {
        notificationQueue.add(notification);
    }
}
