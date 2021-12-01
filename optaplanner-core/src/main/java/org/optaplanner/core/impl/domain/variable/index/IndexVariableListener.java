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

import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class IndexVariableListener<Solution_> implements ListVariableListener<Solution_, Object>, IndexVariableSupply {

    private static final int UNSET_ALL = -1;

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
        // TODO null, or *, or the added entity should already have its shadow vars up to date?
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, 0);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, Integer index) {
        retract((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void afterVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, Integer index) {
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        retract((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, UNSET_ALL);
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    protected void insert(InnerScoreDirector<Solution_, ?> scoreDirector, Object sourceEntity, Integer index) {
        List<Object> listVariable = sourceListVariableDescriptor.getListVariable(sourceEntity);
        if (listVariable == null) {
            return;
        }

        for (int i = index; i < listVariable.size(); i++) {
            // TODO maybe update inverse relation variable (if exists) to avoid an extra loop
            //  in a dedicated inverse relation listener
            Object shadowEntity = listVariable.get(i);
            //            if (Objects.equals(indexShadowVariableDescriptor.getValue(shadowEntity), i)) {
            //                // CAUTION! if the inserted element happens to already have the index, that must not shortcut updating the rest.
            //                // Shortcut.
            //                return;
            //            }
            scoreDirector.beforeVariableChanged(indexShadowVariableDescriptor, shadowEntity);
            indexShadowVariableDescriptor.setValue(shadowEntity, i);
            scoreDirector.afterVariableChanged(indexShadowVariableDescriptor, shadowEntity);
        }
    }

    protected void retract(InnerScoreDirector<Solution_, ?> scoreDirector, Object sourceEntity, Integer index) {
        List<Object> listVariable = sourceListVariableDescriptor.getListVariable(sourceEntity);
        if (listVariable == null) {
            return;
        }
        if (index >= listVariable.size()) {
            return;
        }
        // TODO refactor to a separate method
        if (index == UNSET_ALL) {
            for (Object shadowEntity : listVariable) {
                scoreDirector.beforeVariableChanged(indexShadowVariableDescriptor, shadowEntity);
                indexShadowVariableDescriptor.setValue(shadowEntity, null);
                scoreDirector.afterVariableChanged(indexShadowVariableDescriptor, shadowEntity);
            }
            return;
        }

        Object shadowEntity = listVariable.get(index);
        scoreDirector.beforeVariableChanged(indexShadowVariableDescriptor, shadowEntity);
        indexShadowVariableDescriptor.setValue(shadowEntity, null);
        scoreDirector.afterVariableChanged(indexShadowVariableDescriptor, shadowEntity);
    }

    @Override
    public Integer getIndex(Object planningValue) {
        return (Integer) indexShadowVariableDescriptor.getValue(planningValue);
    }
}
