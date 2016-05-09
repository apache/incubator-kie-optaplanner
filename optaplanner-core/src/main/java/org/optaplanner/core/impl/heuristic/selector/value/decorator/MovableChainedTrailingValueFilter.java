/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.value.decorator;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MovableChainedTrailingValueFilter implements SelectionFilter<Object> {

    private final GenuineVariableDescriptor variableDescriptor;

    public MovableChainedTrailingValueFilter(GenuineVariableDescriptor variableDescriptor) {
        this.variableDescriptor = variableDescriptor;
    }

    @Override
    public boolean accept(ScoreDirector scoreDirector, Object value) {
        if (value == null) {
             return true;
        }
        SingletonInverseVariableSupply supply = retrieveSingletonInverseVariableSupply(scoreDirector);
        Object trailingEntity = supply.getInverseSingleton(value);
        EntityDescriptor entityDescriptor = variableDescriptor.getEntityDescriptor();
        if (trailingEntity == null || !entityDescriptor.matchesEntity(trailingEntity)) {
            return true;
        }
        return entityDescriptor.getMovableEntitySelectionFilter().accept(scoreDirector, trailingEntity);
    }

    protected SingletonInverseVariableSupply retrieveSingletonInverseVariableSupply(ScoreDirector scoreDirector) {
        // TODO Performance loss because the supply is retrieved for every accept
        // A SelectionFilter should be optionally made aware of lifecycle events, so it can cache the supply
        SupplyManager supplyManager = ((InnerScoreDirector) scoreDirector).getSupplyManager();
        return supplyManager.demand(new SingletonInverseVariableDemand(variableDescriptor));
    }

}
