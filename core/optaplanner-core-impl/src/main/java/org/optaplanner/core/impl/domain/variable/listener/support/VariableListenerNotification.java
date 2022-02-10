/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.Objects;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;

final class VariableListenerNotification {

    private final Object entity;
    private final VariableListenerNotificationType type;

    public static VariableListenerNotification entityAdded(Object entity) {
        return new VariableListenerNotification(entity, VariableListenerNotificationType.ENTITY_ADDED);
    }

    public static VariableListenerNotification variableChanged(Object entity) {
        return new VariableListenerNotification(entity, VariableListenerNotificationType.VARIABLE_CHANGED);
    }

    public static VariableListenerNotification entityRemoved(Object entity) {
        return new VariableListenerNotification(entity, VariableListenerNotificationType.ENTITY_REMOVED);
    }

    private VariableListenerNotification(Object entity, VariableListenerNotificationType type) {
        this.entity = entity;
        this.type = type;
    }

    public <Solution_> void notifyBefore(VariableListener<Solution_, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        switch (type) {
            case ENTITY_ADDED:
                variableListener.beforeEntityAdded(scoreDirector, entity);
                break;
            case VARIABLE_CHANGED:
                variableListener.beforeVariableChanged(scoreDirector, entity);
                break;
            case ENTITY_REMOVED:
                variableListener.beforeEntityRemoved(scoreDirector, entity);
                break;
            default:
                throw new IllegalStateException("The VariableListenerNotification type (" + type + ") is not implemented.");
        }
    }

    public <Solution_> void notifyAfter(VariableListener<Solution_, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        switch (type) {
            case ENTITY_ADDED:
                variableListener.afterEntityAdded(scoreDirector, entity);
                break;
            case VARIABLE_CHANGED:
                variableListener.afterVariableChanged(scoreDirector, entity);
                break;
            case ENTITY_REMOVED:
                variableListener.afterEntityRemoved(scoreDirector, entity);
                break;
            default:
                throw new IllegalStateException("The VariableListenerNotification type (" + type + ") is not implemented.");
        }
    }

    /**
     * Warning: do not test equality of {@link VariableListenerNotification}s for different {@link VariableListener}s
     * (so {@link ShadowVariableDescriptor}s) because equality does not take those into account (for performance)!
     *
     * @param o sometimes null
     * @return true if same entity instance and the same type
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof VariableListenerNotification) {
            VariableListenerNotification other = (VariableListenerNotification) o;
            return entity == other.entity && type == other.type;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(entity), type);
    }

    @Override
    public String toString() {
        return type + ": " + entity;
    }
}
