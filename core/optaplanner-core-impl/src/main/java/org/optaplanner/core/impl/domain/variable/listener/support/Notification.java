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

import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public interface Notification<Solution_, T extends AbstractVariableListener<Solution_, Object>> {

    static <Solution_> EntityNotification<Solution_> entityAdded(Object entity) {
        return new EntityAddedNotification<>(entity);
    }

    static <Solution_> EntityNotification<Solution_> entityRemoved(Object entity) {
        return new EntityRemovedNotification<>(entity);
    }

    static <Solution_> BasicVariableNotification<Solution_> variableChanged(Object entity) {
        return new VariableChangedNotification<>(entity);
    }

    static <Solution_> ListVariableNotification<Solution_> listVariableChanged(Object entity) {
        return new ListVariableChangedNotification<>(entity);
    }

    void triggerBefore(T variableListener, ScoreDirector<Solution_> scoreDirector);

    void triggerAfter(T variableListener, ScoreDirector<Solution_> scoreDirector);
}
