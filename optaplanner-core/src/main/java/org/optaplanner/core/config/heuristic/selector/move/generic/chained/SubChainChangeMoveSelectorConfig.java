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
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "entityClass",
        "subChainSelectorConfig",
        "valueSelectorConfig",
        "selectReversingMoveToo"
})
public class SubChainChangeMoveSelectorConfig<Solution_>
        extends MoveSelectorConfig<Solution_, SubChainChangeMoveSelectorConfig<Solution_>> {

    public static final String XML_ELEMENT_NAME = "subChainChangeMoveSelector";

    private Class<?> entityClass = null;
    @XmlElement(name = "subChainSelector")
    private SubChainSelectorConfig<Solution_> subChainSelectorConfig = null;
    @XmlElement(name = "valueSelector")
    private ValueSelectorConfig<Solution_> valueSelectorConfig = null;

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

    public ValueSelectorConfig<Solution_> getValueSelectorConfig() {
        return valueSelectorConfig;
    }

    public void setValueSelectorConfig(ValueSelectorConfig<Solution_> valueSelectorConfig) {
        this.valueSelectorConfig = valueSelectorConfig;
    }

    public Boolean getSelectReversingMoveToo() {
        return selectReversingMoveToo;
    }

    public void setSelectReversingMoveToo(Boolean selectReversingMoveToo) {
        this.selectReversingMoveToo = selectReversingMoveToo;
    }

    @Override
    public SubChainChangeMoveSelectorConfig<Solution_> inherit(
            SubChainChangeMoveSelectorConfig<Solution_> inheritedConfig) {
        super.inherit(inheritedConfig);
        entityClass = ConfigUtils.inheritOverwritableProperty(entityClass, inheritedConfig.getEntityClass());
        subChainSelectorConfig = ConfigUtils.inheritConfig(subChainSelectorConfig, inheritedConfig.getSubChainSelectorConfig());
        valueSelectorConfig = ConfigUtils.inheritConfig(valueSelectorConfig, inheritedConfig.getValueSelectorConfig());
        selectReversingMoveToo = ConfigUtils.inheritOverwritableProperty(selectReversingMoveToo,
                inheritedConfig.getSelectReversingMoveToo());
        return this;
    }

    @Override
    public SubChainChangeMoveSelectorConfig<Solution_> copyConfig() {
        return new SubChainChangeMoveSelectorConfig<Solution_>().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + subChainSelectorConfig + ", " + valueSelectorConfig + ")";
    }

}
