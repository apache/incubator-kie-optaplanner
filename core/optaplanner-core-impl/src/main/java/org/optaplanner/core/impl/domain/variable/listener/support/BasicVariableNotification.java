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

import java.util.function.BiConsumer;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public interface BasicVariableNotification<Solution_>
        extends BiConsumer<VariableListener<Solution_, Object>, ScoreDirector<Solution_>> {

    void triggerBefore(VariableListener<Solution_, Object> variableListener, ScoreDirector<Solution_> scoreDirector);

    void triggerAfter(VariableListener<Solution_, Object> variableListener, ScoreDirector<Solution_> scoreDirector);

    @Override
    default void accept(VariableListener<Solution_, Object> variableListener, ScoreDirector<Solution_> scoreDirector) {
        triggerAfter(variableListener, scoreDirector);
    }

    static <Solution_> BasicVariableNotification<Solution_> variableChanged(Object entity) {
        return new VariableChangedNotification<>(entity);
    }
}
