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

import java.util.function.Consumer;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.change.ProblemChangeDirector;

public final class DefaultProblemChangeDirector<Solution_> implements ProblemChangeDirector {

    private final ScoreDirector<Solution_> scoreDirector;

    public DefaultProblemChangeDirector(ScoreDirector<Solution_> scoreDirector) {
        this.scoreDirector = scoreDirector;
    }

    @Override
    public <Entity> void addEntity(Entity entity, Consumer<Entity> entityConsumer) {
        scoreDirector.beforeEntityAdded(entity);
        entityConsumer.accept(entity);
        scoreDirector.afterEntityAdded(entity);
    }

    @Override
    public <Entity> void removeEntity(Entity entity, Consumer<Entity> workingEntityConsumer) {
        Entity workingEntity = lookUpWorkingObject(entity);
        scoreDirector.beforeEntityRemoved(workingEntity);
        workingEntityConsumer.accept(workingEntity);
        scoreDirector.afterEntityRemoved(workingEntity);
    }

    @Override
    public <Entity> void changeVariable(Entity entity, Consumer<Entity> workingEntityConsumer, String variableName) {
        Entity workingEntity = lookUpWorkingObject(entity);
        scoreDirector.beforeVariableChanged(workingEntity, variableName);
        workingEntityConsumer.accept(workingEntity);
        scoreDirector.afterVariableChanged(workingEntity, variableName);
    }

    @Override
    public <ProblemFact> void addProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer) {
        scoreDirector.beforeProblemFactAdded(problemFact);
        problemFactConsumer.accept(problemFact);
        scoreDirector.afterProblemFactAdded(problemFact);
    }

    @Override
    public <ProblemFact> void removeProblemFact(ProblemFact problemFact, Consumer<ProblemFact> workingProblemFactConsumer) {
        ProblemFact workingProblemFact = lookUpWorkingObject(problemFact);
        scoreDirector.beforeProblemFactRemoved(workingProblemFact);
        workingProblemFactConsumer.accept(workingProblemFact);
        scoreDirector.afterProblemFactRemoved(workingProblemFact);
    }

    @Override
    public <EntityOrProblemFact> void changeProblemProperty(EntityOrProblemFact problemFactOrEntity,
            Consumer<EntityOrProblemFact> workingProblemFactOrEntityConsumer) {
        EntityOrProblemFact workingEntityOrProblemFact = lookUpWorkingObject(problemFactOrEntity);
        scoreDirector.beforeProblemPropertyChanged(workingEntityOrProblemFact);
        workingProblemFactOrEntityConsumer.accept(workingEntityOrProblemFact);
        scoreDirector.afterProblemPropertyChanged(workingEntityOrProblemFact);
    }

    @Override
    public <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObject(EntityOrProblemFact externalObject) {
        return scoreDirector.lookUpWorkingObject(externalObject);
    }

    @Override
    public <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObjectOrReturnNull(EntityOrProblemFact externalObject) {
        return scoreDirector.lookUpWorkingObjectOrReturnNull(externalObject);
    }
}
