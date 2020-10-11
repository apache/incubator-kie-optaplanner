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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "valueSelectorConfig"
})
public class PillarChangeMoveSelectorConfig<Solution_>
        extends AbstractPillarMoveSelectorConfig<Solution_, PillarChangeMoveSelectorConfig<Solution_>> {

    public static final String XML_ELEMENT_NAME = "pillarChangeMoveSelector";

    @XmlElement(name = "valueSelector")
    private ValueSelectorConfig<Solution_> valueSelectorConfig = null;

    public ValueSelectorConfig<Solution_> getValueSelectorConfig() {
        return valueSelectorConfig;
    }

    public void setValueSelectorConfig(ValueSelectorConfig<Solution_> valueSelectorConfig) {
        this.valueSelectorConfig = valueSelectorConfig;
    }

    @Override
    public PillarChangeMoveSelectorConfig<Solution_> inherit(PillarChangeMoveSelectorConfig<Solution_> inheritedConfig) {
        super.inherit(inheritedConfig);
        valueSelectorConfig = ConfigUtils.inheritConfig(valueSelectorConfig, inheritedConfig.getValueSelectorConfig());
        return this;
    }

    @Override
    public PillarChangeMoveSelectorConfig<Solution_> copyConfig() {
        return new PillarChangeMoveSelectorConfig<Solution_>().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pillarSelectorConfig + ", " + valueSelectorConfig + ")";
    }

}
