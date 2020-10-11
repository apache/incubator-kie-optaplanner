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

package org.optaplanner.core.config.heuristic.selector.move.generic;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "entitySelectorConfig",
        "secondaryEntitySelectorConfig",
        "variableNameIncludeList"
})
public class SwapMoveSelectorConfig<Solution_>
        extends MoveSelectorConfig<Solution_, SwapMoveSelectorConfig<Solution_>> {

    public static final String XML_ELEMENT_NAME = "swapMoveSelector";

    @XmlElement(name = "entitySelector")
    private EntitySelectorConfig<Solution_> entitySelectorConfig = null;
    @XmlElement(name = "secondaryEntitySelector")
    private EntitySelectorConfig<Solution_> secondaryEntitySelectorConfig = null;

    @XmlElementWrapper(name = "variableNameIncludes")
    @XmlElement(name = "variableNameInclude")
    private List<String> variableNameIncludeList = null;

    public EntitySelectorConfig<Solution_> getEntitySelectorConfig() {
        return entitySelectorConfig;
    }

    public void setEntitySelectorConfig(EntitySelectorConfig<Solution_> entitySelectorConfig) {
        this.entitySelectorConfig = entitySelectorConfig;
    }

    public EntitySelectorConfig<Solution_> getSecondaryEntitySelectorConfig() {
        return secondaryEntitySelectorConfig;
    }

    public void setSecondaryEntitySelectorConfig(EntitySelectorConfig<Solution_> secondaryEntitySelectorConfig) {
        this.secondaryEntitySelectorConfig = secondaryEntitySelectorConfig;
    }

    public List<String> getVariableNameIncludeList() {
        return variableNameIncludeList;
    }

    public void setVariableNameIncludeList(List<String> variableNameIncludeList) {
        this.variableNameIncludeList = variableNameIncludeList;
    }

    @Override
    public SwapMoveSelectorConfig<Solution_> inherit(SwapMoveSelectorConfig<Solution_> inheritedConfig) {
        super.inherit(inheritedConfig);
        entitySelectorConfig = ConfigUtils.inheritConfig(entitySelectorConfig, inheritedConfig.getEntitySelectorConfig());
        secondaryEntitySelectorConfig = ConfigUtils.inheritConfig(secondaryEntitySelectorConfig,
                inheritedConfig.getSecondaryEntitySelectorConfig());
        variableNameIncludeList = ConfigUtils.inheritMergeableListProperty(
                variableNameIncludeList, inheritedConfig.getVariableNameIncludeList());
        return this;
    }

    @Override
    public SwapMoveSelectorConfig<Solution_> copyConfig() {
        return new SwapMoveSelectorConfig<Solution_>().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entitySelectorConfig
                + (secondaryEntitySelectorConfig == null ? "" : ", " + secondaryEntitySelectorConfig) + ")";
    }

}
