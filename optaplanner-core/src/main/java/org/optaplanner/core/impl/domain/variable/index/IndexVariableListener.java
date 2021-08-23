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

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class IndexVariableListener<Solution_> implements VariableListener<Solution_, Object>, IndexVariableSupply {

    protected final IndexShadowVariableDescriptor<Solution_> indexShadowVariableDescriptor;
    protected final ListVariableDescriptor<Solution_> sourceListVariableDescriptor;

    public IndexVariableListener(
            IndexShadowVariableDescriptor<Solution_> indexShadowVariableDescriptor,
            ListVariableDescriptor<Solution_> sourceListVariableDescriptor) {
        this.indexShadowVariableDescriptor = indexShadowVariableDescriptor;
        this.sourceListVariableDescriptor = sourceListVariableDescriptor;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // TODO maybe set all indexes to null
        // When we support over-constrained planning (e.g. there is not enough Employees to assign all tasks), then
        // an unassigned planning value should have a null index.
    }

    @Override
    public void afterVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity) {
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        retract((InnerScoreDirector<Solution_, ?>) scoreDirector, entity);
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    protected void insert(InnerScoreDirector<Solution_, ?> scoreDirector, Object sourceEntity) {
        List<Object> listVariable = sourceListVariableDescriptor.getListVariable(sourceEntity);
        if (listVariable == null) {
            return;
        }
        int index = 0;
        for (Object shadowEntity : listVariable) {
            // TODO maybe update inverse relation variable (if exists) to avoid an extra loop
            //  in a dedicated inverse relation listener
            if (!Objects.equals(indexShadowVariableDescriptor.getValue(shadowEntity), index)) {
                scoreDirector.beforeVariableChanged(indexShadowVariableDescriptor, shadowEntity);
                indexShadowVariableDescriptor.setValue(shadowEntity, index);
                scoreDirector.afterVariableChanged(indexShadowVariableDescriptor, shadowEntity);
            }
            index++;
        }
    }

    protected void retract(InnerScoreDirector<Solution_, ?> scoreDirector, Object sourceEntity) {
        List<Object> listVariable = sourceListVariableDescriptor.getListVariable(sourceEntity);
        if (listVariable == null) {
            return;
        }
        for (Object shadowEntity : listVariable) {
            scoreDirector.beforeVariableChanged(indexShadowVariableDescriptor, shadowEntity);
            indexShadowVariableDescriptor.setValue(shadowEntity, null);
            scoreDirector.afterVariableChanged(indexShadowVariableDescriptor, shadowEntity);
        }
    }

    @Override
    public Integer getIndex(Object planningValue) {
        return (Integer) indexShadowVariableDescriptor.getValue(planningValue);
    }
}
