/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.config.constructionheuristic.placer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySorterMannerHelper;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSorterMannerHelper;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.constructionheuristic.placer.PooledEntityPlacer;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.solver.termination.Termination;

@XStreamAlias("pooledEntityPlacer")
public class PooledEntityPlacerConfig extends EntityPlacerConfig<PooledEntityPlacerConfig> {

    // TODO This is a List due to XStream limitations. With JAXB it could be just a MoveSelectorConfig instead.
    @XStreamImplicit()
    private List<MoveSelectorConfig> moveSelectorConfigList = null;

    public MoveSelectorConfig getMoveSelectorConfig() {
        return moveSelectorConfigList == null ? null : moveSelectorConfigList.get(0);
    }

    public void setMoveSelectorConfig(MoveSelectorConfig moveSelectorConfig) {
        this.moveSelectorConfigList = moveSelectorConfig == null ? null : Collections.singletonList(moveSelectorConfig);
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    @Override
    public PooledEntityPlacer buildEntityPlacer(HeuristicConfigPolicy configPolicy, Termination phaseTermination) {
        MoveSelectorConfig moveSelectorConfig;
        if (ConfigUtils.isEmptyCollection(moveSelectorConfigList)) {
            moveSelectorConfig = buildMoveSelectorConfig(configPolicy);
        } else if (moveSelectorConfigList.size() == 1) {
            moveSelectorConfig = moveSelectorConfigList.get(0);
        } else {
            // TODO moveSelectorConfigList is only a List because of XStream limitations.
            throw new IllegalArgumentException("The moveSelectorConfigList (" + moveSelectorConfigList
                    + ") must be a singleton or empty. Use a single " + UnionMoveSelectorConfig.class
                    + " or " + CartesianProductMoveSelectorConfig.class
                    + " element to nest multiple MoveSelectors.");
        }
        MoveSelector moveSelector = moveSelectorConfig.buildMoveSelector(
                configPolicy, SelectionCacheType.JUST_IN_TIME, SelectionOrder.ORIGINAL);
        return new PooledEntityPlacer(moveSelector);
    }

    private MoveSelectorConfig buildMoveSelectorConfig(HeuristicConfigPolicy configPolicy) {
        EntityDescriptor entityDescriptor = deduceEntityDescriptor(configPolicy.getSolutionDescriptor(), null);
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
        MoveSelectorConfig moveSelectorConfig;
        if (subMoveSelectorConfigList.size() > 1) {
            if (true) { // TODO
                moveSelectorConfig = new CartesianProductMoveSelectorConfig(subMoveSelectorConfigList);
            } else {
                moveSelectorConfig = new UnionMoveSelectorConfig(subMoveSelectorConfigList);
            }
        } else {
            moveSelectorConfig = subMoveSelectorConfigList.get(0);
        }
        return moveSelectorConfig;
    }

    private EntitySelectorConfig buildEntitySelectorConfig(HeuristicConfigPolicy configPolicy,
            EntityDescriptor entityDescriptor) {
        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        Class<?> entityClass = entityDescriptor.getEntityClass();
        entitySelectorConfig.setId(entityClass.getName());
        entitySelectorConfig.setEntityClass(entityClass);
        if (EntitySorterMannerHelper.hasSorter(configPolicy.getEntitySorterManner(), entityDescriptor)) {
            entitySelectorConfig.setCacheType(SelectionCacheType.PHASE);
            entitySelectorConfig.setSelectionOrder(SelectionOrder.SORTED);
            entitySelectorConfig.setSorterManner(configPolicy.getEntitySorterManner());
        }
        return entitySelectorConfig;
    }

    private ChangeMoveSelectorConfig buildChangeMoveSelectorConfig(HeuristicConfigPolicy configPolicy,
            String entitySelectorConfigId, GenuineVariableDescriptor variableDescriptor) {
        ChangeMoveSelectorConfig changeMoveSelectorConfig = new ChangeMoveSelectorConfig();
        EntitySelectorConfig changeEntitySelectorConfig = new EntitySelectorConfig();
        changeEntitySelectorConfig.setMimicSelectorRef(entitySelectorConfigId);
        changeMoveSelectorConfig.setEntitySelectorConfig(changeEntitySelectorConfig);
        ValueSelectorConfig changeValueSelectorConfig = new ValueSelectorConfig();
        changeValueSelectorConfig.setVariableName(variableDescriptor.getVariableName());
        if (ValueSorterMannerHelper.hasSorter(configPolicy.getValueSorterManner(), variableDescriptor)) {
            if (variableDescriptor.isValueRangeEntityIndependent()) {
                changeValueSelectorConfig.setCacheType(SelectionCacheType.PHASE);
            } else {
                changeValueSelectorConfig.setCacheType(SelectionCacheType.STEP);
            }
            changeValueSelectorConfig.setSelectionOrder(SelectionOrder.SORTED);
            changeValueSelectorConfig.setSorterManner(configPolicy.getValueSorterManner());
        }
        changeMoveSelectorConfig.setValueSelectorConfig(changeValueSelectorConfig);
        return changeMoveSelectorConfig;
    }

    @Override
    public void inherit(PooledEntityPlacerConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        setMoveSelectorConfig(ConfigUtils.inheritOverwritableProperty(
                getMoveSelectorConfig(), inheritedConfig.getMoveSelectorConfig()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getMoveSelectorConfig() + ")";
    }

}
