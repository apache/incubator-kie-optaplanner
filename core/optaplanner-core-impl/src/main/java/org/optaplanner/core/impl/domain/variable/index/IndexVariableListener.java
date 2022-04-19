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

package org.optaplanner.core.impl.domain.variable.index;

import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class IndexVariableListener<Solution_> implements ListVariableListener<Solution_, Object>, IndexVariableSupply {

    protected final IndexShadowVariableDescriptor<Solution_> shadowVariableDescriptor;
    protected final ListVariableDescriptor<Solution_> sourceVariableDescriptor;

    public IndexVariableListener(
            IndexShadowVariableDescriptor<Solution_> shadowVariableDescriptor,
            ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.shadowVariableDescriptor = shadowVariableDescriptor;
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        updateIndexes((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, 0);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        for (Object element : listVariable) {
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public void beforeElementAdded(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        // Do nothing
    }

    @Override
    public void afterElementAdded(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        updateIndexes((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void beforeElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        Object element = sourceVariableDescriptor.getElement(entity, index);
        innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
        shadowVariableDescriptor.setValue(element, null);
        innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
    }

    @Override
    public void afterElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        updateIndexes((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void beforeElementMoved(ScoreDirector<Solution_> scoreDirector,
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex) {
        // Do nothing
    }

    @Override
    public void afterElementMoved(ScoreDirector<Solution_> scoreDirector,
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        if (sourceEntity == destinationEntity) {
            if (destinationIndex < sourceIndex) {
                updateIndexes(innerScoreDirector, sourceEntity, destinationIndex, sourceIndex + 1);
            } else {
                updateIndexes(innerScoreDirector, sourceEntity, sourceIndex, destinationIndex + 1);
            }
        } else {
            updateIndexes(innerScoreDirector, sourceEntity, sourceIndex);
            updateIndexes(innerScoreDirector, destinationEntity,
                    destinationIndex == sourceIndex ? destinationIndex + 1 : destinationIndex);
        }
    }

    private void updateIndexes(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity, int index) {
        updateIndexes(scoreDirector, entity, index, Integer.MAX_VALUE);
    }

    private void updateIndexes(InnerScoreDirector<Solution_, ?> scoreDirector, Object entity, int startIndex, int endIndex) {
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        for (int i = startIndex; i < Math.min(endIndex, listVariable.size()); i++) {
            Object element = listVariable.get(i);
            Integer oldIndex = shadowVariableDescriptor.getValue(element);
            if (Objects.equals(oldIndex, i)) {
                return;
            }
            scoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, i);
            scoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public Integer getIndex(Object planningValue) {
        return shadowVariableDescriptor.getValue(planningValue);
    }
}
