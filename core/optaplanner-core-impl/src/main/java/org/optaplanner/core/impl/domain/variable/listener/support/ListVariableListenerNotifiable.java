/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.variable.listener.support;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

final class ListVariableListenerNotifiable<Solution_> extends AbstractNotifiable<Solution_> {

    private final ListVariableListener<Solution_, Object> variableListener;
    private final Collection<BiConsumer<? super ListVariableListener<Solution_, Object>, ScoreDirector<Solution_>>> notificationQueue;

    ListVariableListenerNotifiable(
            ScoreDirector<Solution_> scoreDirector,
            ListVariableListener<Solution_, Object> variableListener,
            int globalOrder) {
        super(scoreDirector, globalOrder);
        this.variableListener = variableListener;
        if (variableListener.requiresUniqueEntityEvents()) {
            notificationQueue = new SmallScalingOrderedSet<>();
        } else {
            notificationQueue = new ArrayDeque<>();
        }
    }

    @Override
    AbstractVariableListener<Solution_, ?> getVariableListener() {
        return variableListener;
    }

    void addNotification(ListVariableNotification<Solution_> notification) {
        if (notificationQueue.add(notification)) {
            notification.triggerBefore(variableListener, scoreDirector);
        }
    }

    @Override
    public void addNotification(EntityNotification<Solution_> notification) {
        if (notificationQueue.add(notification)) {
            notification.triggerBefore(variableListener, scoreDirector);
        }
    }

    @Override
    void triggerAllNotifications() {
        int notifiedCount = 0;
        for (BiConsumer<? super ListVariableListener<Solution_, Object>, ScoreDirector<Solution_>> notification : notificationQueue) {
            notification.accept(variableListener, scoreDirector);
            notifiedCount++;
        }
        if (notifiedCount != notificationQueue.size()) {
            throw new IllegalStateException("The variableListener (" + variableListener.getClass()
                    + ") has been notified with notifiedCount (" + notifiedCount
                    + ") but after being triggered, its notificationCount (" + notificationQueue.size()
                    + ") is different.\n"
                    + "Maybe that variableListener (" + variableListener.getClass()
                    + ") changed an upstream shadow variable (which is illegal).");
        }
        notificationQueue.clear();
    }
}
