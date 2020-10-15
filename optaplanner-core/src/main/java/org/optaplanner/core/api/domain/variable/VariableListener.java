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

package org.optaplanner.core.api.domain.variable;

import java.io.Closeable;

import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Changes shadow variables when a genuine planning variable changes.
 * <p>
 * Important: it must only change the shadow variable(s) for which it's configured!
 * It should never change a genuine variable or a problem fact.
 * It can change its shadow variable(s) on multiple entity instances
 * (for example: an arrivalTime change affects all trailing entities too).
 * <p>
 * It is recommended that implementations be kept stateless.
 * If state must be implemented, implementations may need to override the default methods
 * ({@link #resetWorkingSolution(ScoreDirector)}, {@link #close()}).
 *
 * @param <Entity_> @{@link PlanningEntity} on which the variable is declared
 */
public interface VariableListener<Entity_> extends Closeable {

    /**
     * When set to {@code true}, this has a slight performance loss in Planner.
     * When set to {@code false}, it's often easier to make the listener implementation correct and fast.
     *
     * @return true to guarantee that each of the before/after methods will only be called once per entity instance
     *         per operation type (add, change or remove).
     */
    default boolean requiresUniqueEntityEvents() {
        return false;
    }

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void beforeEntityAdded(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void afterEntityAdded(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void beforeVariableChanged(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void afterVariableChanged(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void beforeEntityRemoved(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * @param scoreDirector never null
     * @param entity never null
     */
    void afterEntityRemoved(ScoreDirector scoreDirector, Entity_ entity);

    /**
     * Called when the entire working solution changes. In this event, the other before..()/after...() methods will not
     * be called.
     * At this point, implementations should clear state, if any.
     *
     * @param scoreDirector never null
     */
    default void resetWorkingSolution(ScoreDirector scoreDirector) {
        // No need to do anything for stateless variable listeners.
    }

    @Override
    default void close() {
        // No need to do anything for stateless variable listeners.
    }

}
