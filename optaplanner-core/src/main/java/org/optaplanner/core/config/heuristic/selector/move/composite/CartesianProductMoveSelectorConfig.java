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

package org.optaplanner.core.config.heuristic.selector.move.composite;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.composite.CartesianProductMoveSelector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("cartesianProductMoveSelector")
public class CartesianProductMoveSelectorConfig extends MoveSelectorConfig<CartesianProductMoveSelectorConfig> {

    @XmlElements({
            @XmlElement(name = "unionMoveSelector", type = UnionMoveSelectorConfig.class),
            @XmlElement(name = "cartesianProductMoveSelector", type = CartesianProductMoveSelectorConfig.class),
            @XmlElement(name = "changeMoveSelector", type = ChangeMoveSelectorConfig.class),
    })
    @XStreamImplicit()
    private List<MoveSelectorConfig> moveSelectorConfigList = null;

    private Boolean ignoreEmptyChildIterators = null;

    public CartesianProductMoveSelectorConfig() {
    }

    public CartesianProductMoveSelectorConfig(List<MoveSelectorConfig> moveSelectorConfigList) {
        this.moveSelectorConfigList = moveSelectorConfigList;
    }

    public List<MoveSelectorConfig> getMoveSelectorConfigList() {
        return moveSelectorConfigList;
    }

    public void setMoveSelectorConfigList(List<MoveSelectorConfig> moveSelectorConfigList) {
        this.moveSelectorConfigList = moveSelectorConfigList;
    }

    public Boolean getIgnoreEmptyChildIterators() {
        return ignoreEmptyChildIterators;
    }

    public void setIgnoreEmptyChildIterators(Boolean ignoreEmptyChildIterators) {
        this.ignoreEmptyChildIterators = ignoreEmptyChildIterators;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    @Override
    public MoveSelector buildBaseMoveSelector(HeuristicConfigPolicy configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        List<MoveSelector> moveSelectorList = new ArrayList<>(moveSelectorConfigList.size());
        for (MoveSelectorConfig moveSelectorConfig : moveSelectorConfigList) {
            moveSelectorList.add(
                    moveSelectorConfig.buildMoveSelector(configPolicy,
                            minimumCacheType, SelectionOrder.fromRandomSelectionBoolean(randomSelection)));
        }
        boolean ignoreEmptyChildIterators_ = defaultIfNull(ignoreEmptyChildIterators, true);
        return new CartesianProductMoveSelector(moveSelectorList, ignoreEmptyChildIterators_, randomSelection);
    }

    @Override
    public void extractLeafMoveSelectorConfigsIntoList(List<MoveSelectorConfig> leafMoveSelectorConfigList) {
        for (MoveSelectorConfig moveSelectorConfig : moveSelectorConfigList) {
            moveSelectorConfig.extractLeafMoveSelectorConfigsIntoList(leafMoveSelectorConfigList);
        }
    }

    @Override
    public CartesianProductMoveSelectorConfig inherit(CartesianProductMoveSelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        moveSelectorConfigList = ConfigUtils.inheritMergeableListConfig(
                moveSelectorConfigList, inheritedConfig.getMoveSelectorConfigList());
        ignoreEmptyChildIterators = ConfigUtils.inheritOverwritableProperty(
                ignoreEmptyChildIterators, inheritedConfig.getIgnoreEmptyChildIterators());
        return this;
    }

    @Override
    public CartesianProductMoveSelectorConfig copyConfig() {
        return new CartesianProductMoveSelectorConfig().inherit(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + moveSelectorConfigList + ")";
    }

}
