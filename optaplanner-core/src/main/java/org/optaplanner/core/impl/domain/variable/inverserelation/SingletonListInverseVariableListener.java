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

package org.optaplanner.core.impl.domain.variable.inverserelation;

import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class SingletonListInverseVariableListener<Solution_>
        implements VariableListener<Solution_, Object>,
        ListVariableListener<Solution_, Object>,
        SingletonInverseVariableSupply {

    protected final InverseRelationShadowVariableDescriptor<Solution_> shadowVariableDescriptor;
    protected final ListVariableDescriptor<Solution_> sourceListVariableDescriptor;

    public SingletonListInverseVariableListener(
            InverseRelationShadowVariableDescriptor<Solution_> shadowVariableDescriptor,
            ListVariableDescriptor<Solution_> sourceListVariableDescriptor) {
        this.shadowVariableDescriptor = shadowVariableDescriptor;
        this.sourceListVariableDescriptor = sourceListVariableDescriptor;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // FIXME avoid NPE
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, null);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, Integer index) {
        //        logger.debug("Inverse:BEFORE {}[{}]", entity, index);
        retract((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void afterVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, Integer index) {
        //        logger.debug("Inverse:AFTER {}[{}]", entity, index);
        insert((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, index);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // FIXME avoid NPE
        retract((InnerScoreDirector<Solution_, ?>) scoreDirector, entity, null);
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
        if (index >= listVariable.size()) {
            return;
        }
        Object shadowEntity = listVariable.get(index);
        if (!Objects.equals(shadowVariableDescriptor.getValue(shadowEntity), sourceEntity)) {
            scoreDirector.beforeVariableChanged(shadowVariableDescriptor, shadowEntity);
            shadowVariableDescriptor.setValue(shadowEntity, sourceEntity);
            scoreDirector.afterVariableChanged(shadowVariableDescriptor, shadowEntity);
            if (index + 1 < listVariable.size()) {
                Object nextEntity = listVariable.get(index + 1);
                scoreDirector.beforeVariableChanged(shadowVariableDescriptor, nextEntity);
                shadowVariableDescriptor.setValue(nextEntity, sourceEntity);
                scoreDirector.afterVariableChanged(shadowVariableDescriptor, nextEntity);
            }
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
        Object shadowEntity = listVariable.get(index);
        scoreDirector.beforeVariableChanged(shadowVariableDescriptor, shadowEntity);
        shadowVariableDescriptor.setValue(shadowEntity, null);
        scoreDirector.afterVariableChanged(shadowVariableDescriptor, shadowEntity);
    }

    @Override
    public Object getInverseSingleton(Object planningValue) {
        return shadowVariableDescriptor.getValue(planningValue);
    }
}
