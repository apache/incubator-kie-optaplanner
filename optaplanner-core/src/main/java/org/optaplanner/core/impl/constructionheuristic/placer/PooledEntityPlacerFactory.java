/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.constructionheuristic.placer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.optaplanner.core.config.constructionheuristic.placer.PooledEntityPlacerConfig;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelectorFactory;

public class PooledEntityPlacerFactory extends AbstractEntityPlacerFactory<PooledEntityPlacerConfig> {

    public PooledEntityPlacerFactory(PooledEntityPlacerConfig placerConfig) {
        super(placerConfig);
    }

    @Override
    public PooledEntityPlacer buildEntityPlacer(HeuristicConfigPolicy configPolicy) {
        MoveSelectorConfig moveSelectorConfig_ =
                placerConfig.getMoveSelectorConfig() == null ? buildMoveSelectorConfig(configPolicy)
                        : placerConfig.getMoveSelectorConfig();

        MoveSelector moveSelector = MoveSelectorFactory.create(moveSelectorConfig_).buildMoveSelector(
                configPolicy, SelectionCacheType.JUST_IN_TIME, SelectionOrder.ORIGINAL);
        return new PooledEntityPlacer(moveSelector);
    }

    private MoveSelectorConfig buildMoveSelectorConfig(HeuristicConfigPolicy configPolicy) {
        EntityDescriptor entityDescriptor = configPolicy.getSolutionDescriptor().deduceEntityDescriptor(null);
        EntitySelectorConfig entitySelectorConfig = buildEntitySelectorConfig(configPolicy, entityDescriptor);

        Collection<GenuineVariableDescriptor> variableDescriptors = entityDescriptor.getGenuineVariableDescriptors();
        List<MoveSelectorConfig> subMoveSelectorConfigList = new ArrayList<>(
                variableDescriptors.size());
        for (GenuineVariableDescriptor variableDescriptor : variableDescriptors) {
            subMoveSelectorConfigList.add(buildChangeMoveSelectorConfig(
                    configPolicy, entitySelectorConfig.getId(), variableDescriptor));
        }
        // The first entitySelectorConfig must be the mimic recorder, not the mimic replayer
        ((ChangeMoveSelectorConfig) subMoveSelectorConfigList.get(0)).setEntitySelectorConfig(entitySelectorConfig);
        MoveSelectorConfig moveSelectorConfig_;
        if (subMoveSelectorConfigList.size() > 1) {
            // Default to cartesian product (not a union) of planning variables.
            moveSelectorConfig_ = new CartesianProductMoveSelectorConfig(subMoveSelectorConfigList);
        } else {
            moveSelectorConfig_ = subMoveSelectorConfigList.get(0);
        }
        return moveSelectorConfig_;
    }

    private EntitySelectorConfig buildEntitySelectorConfig(HeuristicConfigPolicy configPolicy,
            EntityDescriptor entityDescriptor) {
        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        Class<?> entityClass = entityDescriptor.getEntityClass();
        entitySelectorConfig.setId(entityClass.getName());
        entitySelectorConfig.setEntityClass(entityClass);
        if (EntitySelectorConfig.hasSorter(configPolicy.getEntitySorterManner(), entityDescriptor)) {
            entitySelectorConfig.setCacheType(SelectionCacheType.PHASE);
            entitySelectorConfig.setSelectionOrder(SelectionOrder.SORTED);
            entitySelectorConfig.setSorterManner(configPolicy.getEntitySorterManner());
        }
        return entitySelectorConfig;
    }
}
