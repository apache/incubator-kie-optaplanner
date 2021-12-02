/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.solver.change;

import java.util.function.Consumer;

import org.optaplanner.core.api.domain.lookup.LookUpStrategyType;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.PlanningSolution;

public interface ProblemChangeDirector {

    <Entity> void addEntity(Entity entity, Consumer<Entity> entityConsumer);

    <Entity> void removeEntity(Entity entity, Consumer<Entity> entityConsumer);

    <Entity> void changeVariable(Entity entity, Consumer<Entity> entityConsumer, String variableName);

    <ProblemFact> void addProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer);

    <ProblemFact> void removeProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer);

    <EntityOrProblemFact> void changeProblemProperty(EntityOrProblemFact problemFactOrEntity,
            Consumer<EntityOrProblemFact> problemFactOrEntityConsumer);

    /**
     * Translates an entity or fact instance (often from another {@link Thread} or JVM)
     * to this {@link ProblemChangeDirector}'s internal working instance.
     * <p>
     * Matching is determined by the {@link LookUpStrategyType} on {@link PlanningSolution}.
     * Matching uses a {@link PlanningId} by default.
     *
     * @param externalObject sometimes null
     * @return null if externalObject is null
     * @throws IllegalArgumentException if there is no workingObject for externalObject, if it cannot be looked up
     *         or if the externalObject's class is not supported
     * @throws IllegalStateException if it cannot be looked up
     * @param <EntityOrProblemFact> the object type
     */
    <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObject(EntityOrProblemFact externalObject);

    /**
     * As defined by {@link #lookUpWorkingObject(Object)},
     * but doesn't fail fast if no workingObject was ever added for the externalObject.
     * It's recommended to use {@link #lookUpWorkingObject(Object)} instead.
     *
     * @param externalObject sometimes null
     * @return null if externalObject is null or if there is no workingObject for externalObject
     * @throws IllegalArgumentException if it cannot be looked up or if the externalObject's class is not supported
     * @throws IllegalStateException if it cannot be looked up
     * @param <EntityOrProblemFact> the object type
     */
    <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObjectOrReturnNull(EntityOrProblemFact externalObject);
}
