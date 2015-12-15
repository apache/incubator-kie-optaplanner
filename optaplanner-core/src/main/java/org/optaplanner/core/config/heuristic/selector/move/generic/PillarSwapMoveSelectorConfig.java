/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.config.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.pillar.PillarSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.entity.pillar.PillarSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarSwapMoveSelector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@XStreamAlias("pillarSwapMoveSelector")
public class PillarSwapMoveSelectorConfig extends MoveSelectorConfig<PillarSwapMoveSelectorConfig> {

    @XStreamAlias("pillarSelector")
    private PillarSelectorConfig pillarSelectorConfig = null;
    @XStreamAlias("secondaryPillarSelector")
    private PillarSelectorConfig secondaryPillarSelectorConfig = null;

    // TODO jaxb use @XmlElementWrapper and wrap in variableNameIncludes
    @XStreamImplicit(itemFieldName = "variableNameInclude")
    private List<String> variableNameIncludeList = null;

    public PillarSelectorConfig getPillarSelectorConfig() {
        return pillarSelectorConfig;
    }

    public void setPillarSelectorConfig(PillarSelectorConfig pillarSelectorConfig) {
        this.pillarSelectorConfig = pillarSelectorConfig;
    }

    public PillarSelectorConfig getSecondaryPillarSelectorConfig() {
        return secondaryPillarSelectorConfig;
    }

    public void setSecondaryPillarSelectorConfig(PillarSelectorConfig secondaryPillarSelectorConfig) {
        this.secondaryPillarSelectorConfig = secondaryPillarSelectorConfig;
    }

    public List<String> getVariableNameIncludeList() {
        return variableNameIncludeList;
    }

    public void setVariableNameIncludeList(List<String> variableNameIncludeList) {
        this.variableNameIncludeList = variableNameIncludeList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public MoveSelector buildBaseMoveSelector(HeuristicConfigPolicy configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        PillarSelectorConfig pillarSelectorConfig_ = pillarSelectorConfig == null ? new PillarSelectorConfig()
                : pillarSelectorConfig;
        PillarSelector leftPillarSelector = pillarSelectorConfig_.buildPillarSelector(configPolicy,
                minimumCacheType, SelectionOrder.fromRandomSelectionBoolean(randomSelection), variableNameIncludeList);
        PillarSelectorConfig rightPillarSelectorConfig = defaultIfNull(secondaryPillarSelectorConfig, pillarSelectorConfig_);
        PillarSelector rightPillarSelector = rightPillarSelectorConfig.buildPillarSelector(configPolicy,
                minimumCacheType, SelectionOrder.fromRandomSelectionBoolean(randomSelection), variableNameIncludeList);
        Collection<GenuineVariableDescriptor> variableDescriptors = deduceVariableDescriptorList(
                leftPillarSelector.getEntityDescriptor(), variableNameIncludeList);
        return new PillarSwapMoveSelector(leftPillarSelector, rightPillarSelector, variableDescriptors,
                randomSelection);
    }

    public void inherit(PillarSwapMoveSelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        pillarSelectorConfig = ConfigUtils.inheritConfig(pillarSelectorConfig, inheritedConfig.getPillarSelectorConfig());
        secondaryPillarSelectorConfig = ConfigUtils.inheritConfig(secondaryPillarSelectorConfig, inheritedConfig.getSecondaryPillarSelectorConfig());
        variableNameIncludeList = ConfigUtils.inheritMergeableListProperty(
                variableNameIncludeList, inheritedConfig.getVariableNameIncludeList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pillarSelectorConfig
                + (secondaryPillarSelectorConfig == null ? "" : ", " + secondaryPillarSelectorConfig) + ")";
    }

}
