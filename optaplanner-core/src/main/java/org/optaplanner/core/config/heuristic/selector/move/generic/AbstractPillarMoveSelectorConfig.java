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

import java.util.Comparator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.entity.pillar.PillarSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;

@XmlType(propOrder = {
        "subPillarType",
        "subPillarSequenceComparatorClass",
        "pillarSelectorConfig"
})
public abstract class AbstractPillarMoveSelectorConfig<Solution_, C extends AbstractPillarMoveSelectorConfig<Solution_, C>>
        extends MoveSelectorConfig<Solution_, C> {

    protected SubPillarType subPillarType = null;
    protected Class<? extends Comparator> subPillarSequenceComparatorClass = null;
    @XmlElement(name = "pillarSelector")
    protected PillarSelectorConfig<Solution_> pillarSelectorConfig = null;

    public SubPillarType getSubPillarType() {
        return subPillarType;
    }

    public void setSubPillarType(final SubPillarType subPillarType) {
        this.subPillarType = subPillarType;
    }

    public Class<? extends Comparator> getSubPillarSequenceComparatorClass() {
        return subPillarSequenceComparatorClass;
    }

    public void setSubPillarSequenceComparatorClass(final Class<? extends Comparator> subPillarSequenceComparatorClass) {
        this.subPillarSequenceComparatorClass = subPillarSequenceComparatorClass;
    }

    public PillarSelectorConfig<Solution_> getPillarSelectorConfig() {
        return pillarSelectorConfig;
    }

    public void setPillarSelectorConfig(PillarSelectorConfig<Solution_> pillarSelectorConfig) {
        this.pillarSelectorConfig = pillarSelectorConfig;
    }

    @Override
    public C inherit(C inheritedConfig) {
        super.inherit(inheritedConfig);
        subPillarType = ConfigUtils.inheritOverwritableProperty(subPillarType, inheritedConfig.getSubPillarType());
        subPillarSequenceComparatorClass = ConfigUtils.inheritOverwritableProperty(subPillarSequenceComparatorClass,
                inheritedConfig.getSubPillarSequenceComparatorClass());
        pillarSelectorConfig = ConfigUtils.inheritConfig(pillarSelectorConfig, inheritedConfig.getPillarSelectorConfig());
        return (C) this;
    }

}
