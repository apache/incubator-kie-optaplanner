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

import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

/**
 * THIS CLASS IS EXPERIMENTAL AND UNSUPPORTED.
 * Backward compatibility is not guaranteed.
 * It's NOT DOCUMENTED because we'll only document it when it actually works in more than 1 use case.
 *
 * Do not use.
 *
 * @see TailChainSwapMoveSelectorConfig
 */
@XmlType(propOrder = {
        "entitySelectorConfig",
        "valueSelectorConfig"
})
public class KOptMoveSelectorConfig<Solution_>
        extends MoveSelectorConfig<Solution_, KOptMoveSelectorConfig<Solution_>> {

    public static final String XML_ELEMENT_NAME = "kOptMoveSelector";

    @XmlElement(name = "entitySelector")
    private EntitySelectorConfig<Solution_> entitySelectorConfig = null;
    /**
     * Like {@link TailChainSwapMoveSelectorConfig#valueSelectorConfig} but used multiple times to create 1 move.
     */
    @XmlElement(name = "valueSelector")
    private ValueSelectorConfig<Solution_> valueSelectorConfig = null;

    public EntitySelectorConfig<Solution_> getEntitySelectorConfig() {
        return entitySelectorConfig;
    }

    public void setEntitySelectorConfig(EntitySelectorConfig<Solution_> entitySelectorConfig) {
        this.entitySelectorConfig = entitySelectorConfig;
    }

    public ValueSelectorConfig<Solution_> getValueSelectorConfig() {
        return valueSelectorConfig;
    }

    public void setValueSelectorConfig(ValueSelectorConfig<Solution_> valueSelectorConfig) {
        this.valueSelectorConfig = valueSelectorConfig;
    }

    @Override
    public KOptMoveSelectorConfig<Solution_> inherit(KOptMoveSelectorConfig<Solution_> inheritedConfig) {
        super.inherit(inheritedConfig);
        entitySelectorConfig = ConfigUtils.inheritConfig(entitySelectorConfig, inheritedConfig.getEntitySelectorConfig());
        valueSelectorConfig = ConfigUtils.inheritConfig(valueSelectorConfig, inheritedConfig.getValueSelectorConfig());
        return this;
    }

    @Override
    public KOptMoveSelectorConfig<Solution_> copyConfig() {
        return new KOptMoveSelectorConfig<Solution_>().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entitySelectorConfig + ", " + valueSelectorConfig + ")";
    }

}
