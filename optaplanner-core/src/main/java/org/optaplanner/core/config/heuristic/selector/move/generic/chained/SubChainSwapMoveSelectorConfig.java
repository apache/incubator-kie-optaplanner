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

package org.optaplanner.core.config.heuristic.selector.move.generic.chained;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "entityClass",
        "subChainSelectorConfig",
        "secondarySubChainSelectorConfig",
        "selectReversingMoveToo"
})
public class SubChainSwapMoveSelectorConfig<Solution_>
        extends MoveSelectorConfig<Solution_, SubChainSwapMoveSelectorConfig<Solution_>> {

    public static final String XML_ELEMENT_NAME = "subChainSwapMoveSelector";

    private Class<?> entityClass = null;
    @XmlElement(name = "subChainSelector")
    private SubChainSelectorConfig<Solution_> subChainSelectorConfig = null;
    @XmlElement(name = "secondarySubChainSelector")
    private SubChainSelectorConfig<Solution_> secondarySubChainSelectorConfig = null;

    private Boolean selectReversingMoveToo = null;

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public SubChainSelectorConfig<Solution_> getSubChainSelectorConfig() {
        return subChainSelectorConfig;
    }

    public void setSubChainSelectorConfig(SubChainSelectorConfig<Solution_> subChainSelectorConfig) {
        this.subChainSelectorConfig = subChainSelectorConfig;
    }

    public SubChainSelectorConfig<Solution_> getSecondarySubChainSelectorConfig() {
        return secondarySubChainSelectorConfig;
    }

    public void setSecondarySubChainSelectorConfig(SubChainSelectorConfig<Solution_> secondarySubChainSelectorConfig) {
        this.secondarySubChainSelectorConfig = secondarySubChainSelectorConfig;
    }

    public Boolean getSelectReversingMoveToo() {
        return selectReversingMoveToo;
    }

    public void setSelectReversingMoveToo(Boolean selectReversingMoveToo) {
        this.selectReversingMoveToo = selectReversingMoveToo;
    }

    @Override
    public SubChainSwapMoveSelectorConfig<Solution_> inherit(SubChainSwapMoveSelectorConfig<Solution_> inheritedConfig) {
        super.inherit(inheritedConfig);
        entityClass = ConfigUtils.inheritOverwritableProperty(entityClass, inheritedConfig.getEntityClass());
        subChainSelectorConfig = ConfigUtils.inheritConfig(subChainSelectorConfig, inheritedConfig.getSubChainSelectorConfig());
        secondarySubChainSelectorConfig = ConfigUtils.inheritConfig(secondarySubChainSelectorConfig,
                inheritedConfig.getSecondarySubChainSelectorConfig());
        selectReversingMoveToo = ConfigUtils.inheritOverwritableProperty(selectReversingMoveToo,
                inheritedConfig.getSelectReversingMoveToo());
        return this;
    }

    @Override
    public SubChainSwapMoveSelectorConfig<Solution_> copyConfig() {
        return new SubChainSwapMoveSelectorConfig<Solution_>().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + subChainSelectorConfig
                + (secondarySubChainSelectorConfig == null ? "" : ", " + secondarySubChainSelectorConfig) + ")";
    }

}
