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

package org.optaplanner.test.api.change;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.optaplanner.core.api.solver.change.ProblemChangeDirector;

/**
 * Use for unit-testing {@link org.optaplanner.core.api.solver.change.ProblemChange}s.
 */
public class MockProblemChangeDirector implements ProblemChangeDirector {

    private Map<Object, Object> lookUpTable = new IdentityHashMap<>();

    @Override
    public <Entity> void addEntity(Entity entity, Consumer<Entity> entityConsumer) {
        entityConsumer.accept(lookUpWorkingObject(entity));
    }

    @Override
    public <Entity> void removeEntity(Entity entity, Consumer<Entity> entityConsumer) {
        entityConsumer.accept(lookUpWorkingObject(entity));
    }

    @Override
    public <Entity> void changeVariable(Entity entity, String variableName, Consumer<Entity> entityConsumer) {
        entityConsumer.accept(lookUpWorkingObject(entity));
    }

    @Override
    public <ProblemFact> void addProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer) {
        problemFactConsumer.accept(lookUpWorkingObject(problemFact));
    }

    @Override
    public <ProblemFact> void removeProblemFact(ProblemFact problemFact, Consumer<ProblemFact> problemFactConsumer) {
        problemFactConsumer.accept(lookUpWorkingObject(problemFact));
    }

    @Override
    public <EntityOrProblemFact> void changeProblemProperty(EntityOrProblemFact problemFactOrEntity,
            Consumer<EntityOrProblemFact> problemFactOrEntityConsumer) {
        problemFactOrEntityConsumer.accept(lookUpWorkingObject(problemFactOrEntity));
    }

    /**
     * If the look-up result has been provided by a {@link #whenLookingUp(Object)} call, returns the defined object.
     * Otherwise, returns the original externalObject.
     * 
     * @param externalObject entity or problem fact to look up
     */
    @Override
    public <EntityOrProblemFact> EntityOrProblemFact lookUpWorkingObject(EntityOrProblemFact externalObject) {
        EntityOrProblemFact entityOrProblemFact = (EntityOrProblemFact) lookUpTable.get(externalObject);
        return entityOrProblemFact == null ? externalObject : entityOrProblemFact;
    }

    /**
     * If the look-up result has been provided by a {@link #whenLookingUp(Object)} call, returns the defined object.
     * Otherwise, returns null.
     * 
     * @param externalObject entity or problem fact to look up
     */
    @Override
    public <EntityOrProblemFact> Optional<EntityOrProblemFact>
            lookUpWorkingObjectOptionally(EntityOrProblemFact externalObject) {
        return Optional.of((EntityOrProblemFact) lookUpTable.get(externalObject));
    }

    /**
     * Defines what {@link #lookUpWorkingObject(Object)} returns.
     */
    public LookUpMockBuilder whenLookingUp(Object forObject) {
        return new LookUpMockBuilder(forObject);
    }

    public final class LookUpMockBuilder {
        private final Object forObject;

        public LookUpMockBuilder(Object forObject) {
            this.forObject = forObject;
        }

        public MockProblemChangeDirector thenReturn(Object returnObject) {
            lookUpTable.put(forObject, returnObject);
            return MockProblemChangeDirector.this;
        }
    }
}
