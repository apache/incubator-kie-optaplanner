/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.domain.variable.inverserelation;

import java.util.IdentityHashMap;
import java.util.Map;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.SourcedVariableListener;

/**
 * Alternative to {@link SingletonInverseVariableListener}.
 */
public class ExternalizedSingletonInverseVariableSupply<Solution_> implements
        SourcedVariableListener<Solution_>,
        VariableListener<Solution_, Object>,
        SingletonInverseVariableSupply {

    protected final VariableDescriptor<Solution_> sourceVariableDescriptor;

    protected Map<Object, Object> inverseEntityMap = null;

    public ExternalizedSingletonInverseVariableSupply(VariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public VariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(ScoreDirector<Solution_> scoreDirector) {
        inverseEntityMap = new IdentityHashMap<>();
        sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(scoreDirector.getWorkingSolution(), this::insert);
    }

    @Override
    public void close() {
        inverseEntityMap = null;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        insert(entity);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity) {
        retract(entity);
    }

    @Override
    public void afterVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity) {
        insert(entity);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        retract(entity);
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    protected void insert(Object entity) {
        Object value = sourceVariableDescriptor.getValue(entity);
        if (value == null) {
            return;
        }
        Object oldInverseEntity = inverseEntityMap.put(value, entity);
        if (oldInverseEntity != null) {
            throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                    + " because the entity (" + entity
                    + ") for sourceVariable (" + sourceVariableDescriptor.getVariableName()
                    + ") cannot be inserted: another entity (" + oldInverseEntity
                    + ") already has that value (" + value + ").");
        }
    }

    protected void retract(Object entity) {
        Object value = sourceVariableDescriptor.getValue(entity);
        if (value == null) {
            return;
        }
        Object oldInverseEntity = inverseEntityMap.remove(value);
        if (oldInverseEntity != entity) {
            throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                    + " because the entity (" + entity
                    + ") for sourceVariable (" + sourceVariableDescriptor.getVariableName()
                    + ") cannot be retracted: the entity was never inserted for that value (" + value + ").");
        }
    }

    @Override
    public Object getInverseSingleton(Object value) {
        return inverseEntityMap.get(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceVariableDescriptor.getVariableName() + ")";
    }

}
