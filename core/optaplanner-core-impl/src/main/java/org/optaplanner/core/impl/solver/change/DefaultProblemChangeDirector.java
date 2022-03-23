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

package org.optaplanner.core.impl.solver.change;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.optaplanner.core.api.solver.change.ProblemChangeDirector;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public final class DefaultProblemChangeDirector<Solution_> implements ProblemChangeDirector {

    private final InnerScoreDirector<Solution_, ?> scoreDirector;

    public DefaultProblemChangeDirector(InnerScoreDirector<Solution_, ?> scoreDirector) {
        this.scoreDirector = scoreDirector;
    }

    @Override
    public <Entity> void addEntity(Entity entity, Consumer<Entity> entityConsumer) {
        Objects.requireNonNull(entity, () -> "Entity (" + entity + ") cannot be null.");
        Objects.requireNonNull(entityConsumer, () -> "Entity consumer (" + entityConsumer + ") cannot be null.");
        entityConsumer.accept(entity);
    }

    @Override
    public <Entity> void removeEntity(Entity entity, Consumer<Entity> entityConsumer) {
        Objects.requireNonNull(entity, () -> "Entity (" + entity + ") cannot be null.");
        Objects.requireNonNull(entityConsumer, () -> "Entity consumer (" + entityConsumer + ") cannot be null.");
        Entity workingEntity = lookUpWorkingObjectOrFail(entity);
        entityConsumer.accept(workingEntity);
    }

    @Override
    public <Entity> void changeVariable(Entity entity, String variableName, Consumer<Entity> entityConsumer) {
        Objects.requireNonNull(entity, () -> "Entity (" + entity + ") cannot be null.");
        Objects.requireNonNull(variableName, () -> "Planning variable name (" + variableName + ") cannot be null.");
        Objects.requireNonNull(entityConsumer, () -> "Entity consumer (" + entityConsumer + ") cannot be null.");
        Entity workingEntity = lookUpWorkingObjectOrFail(entity);
        entityConsumer.accept(workingEntity);
    }

    @Override
    public <ProblemFact> void addProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer) {
        Objects.requireNonNull(problemFact, () -> "Problem fact (" + problemFact + ") cannot be null.");
        Objects.requireNonNull(problemFactConsumer, () -> "Problem fact consumer (" + problemFactConsumer
                + ") cannot be null.");
        problemFactConsumer.accept(problemFact);
    }

    @Override
    public <ProblemFact> void removeProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer) {
        Objects.requireNonNull(problemFact, () -> "Problem fact (" + problemFact + ") cannot be null.");
        Objects.requireNonNull(problemFactConsumer, () -> "Problem fact consumer (" + problemFactConsumer
                + ") cannot be null.");
        ProblemFact workingProblemFact = lookUpWorkingObjectOrFail(problemFact);
        problemFactConsumer.accept(workingProblemFact);
    }

    @Override
    public <EntityOrProblemFact> void changeProblemProperty(EntityOrProblemFact problemFactOrEntity,
            Consumer<EntityOrProblemFact> problemFactOrEntityConsumer) {
        Objects.requireNonNull(problemFactOrEntity,
                () -> "Problem fact or entity (" + problemFactOrEntity + ") cannot be null.");
        Objects.requireNonNull(problemFactOrEntityConsumer, () -> "Problem fact or entity consumer ("
                + problemFactOrEntityConsumer + ") cannot be null.");
        EntityOrProblemFact workingEntityOrProblemFact = lookUpWorkingObjectOrFail(problemFactOrEntity);
        problemFactOrEntityConsumer.accept(workingEntityOrProblemFact);
    }

    @Override
    public <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObjectOrFail(EntityOrProblemFact externalObject) {
        return scoreDirector.lookUpWorkingObject(externalObject);
    }

    @Override
    public <EntityOrProblemFact> Optional<EntityOrProblemFact>
            lookUpWorkingObject(EntityOrProblemFact externalObject) {
        return Optional.ofNullable(scoreDirector.lookUpWorkingObjectOrReturnNull(externalObject));
    }
}
