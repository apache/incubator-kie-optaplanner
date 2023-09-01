/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.config.heuristic.selector.move;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.SelectorConfig;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveIteratorFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveListFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.kopt.KOptListMoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

/**
 * General superclass for {@link ChangeMoveSelectorConfig}, etc.
 */

@XmlSeeAlso({
        CartesianProductMoveSelectorConfig.class,
        ChangeMoveSelectorConfig.class,
        KOptListMoveSelectorConfig.class,
        ListChangeMoveSelectorConfig.class,
        ListSwapMoveSelectorConfig.class,
        MoveIteratorFactoryConfig.class,
        MoveListFactoryConfig.class,
        PillarChangeMoveSelectorConfig.class,
        PillarSwapMoveSelectorConfig.class,
        SubChainChangeMoveSelectorConfig.class,
        SubChainSwapMoveSelectorConfig.class,
        SubListChangeMoveSelectorConfig.class,
        SubListSwapMoveSelectorConfig.class,
        SwapMoveSelectorConfig.class,
        TailChainSwapMoveSelectorConfig.class,
        UnionMoveSelectorConfig.class
})
@XmlType(propOrder = {
        "cacheType",
        "selectionOrder",
        "filterClass",
        "sorterComparatorClass",
        "sorterWeightFactoryClass",
        "sorterOrder",
        "sorterClass",
        "probabilityWeightFactoryClass",
        "selectedCountLimit",
        "fixedProbabilityWeight"
})
public abstract class MoveSelectorConfig<Config_ extends MoveSelectorConfig<Config_>> extends SelectorConfig<Config_> {

    protected SelectionCacheType cacheType = null;
    protected SelectionOrder selectionOrder = null;

    protected Class<? extends SelectionFilter> filterClass = null;

    protected Class<? extends Comparator> sorterComparatorClass = null;
    protected Class<? extends SelectionSorterWeightFactory> sorterWeightFactoryClass = null;
    protected SelectionSorterOrder sorterOrder = null;
    protected Class<? extends SelectionSorter> sorterClass = null;

    protected Class<? extends SelectionProbabilityWeightFactory> probabilityWeightFactoryClass = null;

    protected Long selectedCountLimit = null;

    private Double fixedProbabilityWeight = null;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public SelectionCacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(SelectionCacheType cacheType) {
        this.cacheType = cacheType;
    }

    public SelectionOrder getSelectionOrder() {
        return selectionOrder;
    }

    public void setSelectionOrder(SelectionOrder selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    public Class<? extends SelectionFilter> getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(Class<? extends SelectionFilter> filterClass) {
        this.filterClass = filterClass;
    }

    public Class<? extends Comparator> getSorterComparatorClass() {
        return sorterComparatorClass;
    }

    public void setSorterComparatorClass(Class<? extends Comparator> sorterComparatorClass) {
        this.sorterComparatorClass = sorterComparatorClass;
    }

    public Class<? extends SelectionSorterWeightFactory> getSorterWeightFactoryClass() {
        return sorterWeightFactoryClass;
    }

    public void setSorterWeightFactoryClass(Class<? extends SelectionSorterWeightFactory> sorterWeightFactoryClass) {
        this.sorterWeightFactoryClass = sorterWeightFactoryClass;
    }

    public SelectionSorterOrder getSorterOrder() {
        return sorterOrder;
    }

    public void setSorterOrder(SelectionSorterOrder sorterOrder) {
        this.sorterOrder = sorterOrder;
    }

    public Class<? extends SelectionSorter> getSorterClass() {
        return sorterClass;
    }

    public void setSorterClass(Class<? extends SelectionSorter> sorterClass) {
        this.sorterClass = sorterClass;
    }

    public Class<? extends SelectionProbabilityWeightFactory> getProbabilityWeightFactoryClass() {
        return probabilityWeightFactoryClass;
    }

    public void setProbabilityWeightFactoryClass(
            Class<? extends SelectionProbabilityWeightFactory> probabilityWeightFactoryClass) {
        this.probabilityWeightFactoryClass = probabilityWeightFactoryClass;
    }

    public Long getSelectedCountLimit() {
        return selectedCountLimit;
    }

    public void setSelectedCountLimit(Long selectedCountLimit) {
        this.selectedCountLimit = selectedCountLimit;
    }

    public Double getFixedProbabilityWeight() {
        return fixedProbabilityWeight;
    }

    public void setFixedProbabilityWeight(Double fixedProbabilityWeight) {
        this.fixedProbabilityWeight = fixedProbabilityWeight;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Config_ withCacheType(SelectionCacheType cacheType) {
        this.cacheType = cacheType;
        return (Config_) this;
    }

    public Config_ withSelectionOrder(SelectionOrder selectionOrder) {
        this.selectionOrder = selectionOrder;
        return (Config_) this;
    }

    public Config_ withFilterClass(Class<? extends SelectionFilter> filterClass) {
        this.filterClass = filterClass;
        return (Config_) this;
    }

    public Config_ withSorterComparatorClass(Class<? extends Comparator> sorterComparatorClass) {
        this.sorterComparatorClass = sorterComparatorClass;
        return (Config_) this;
    }

    public Config_ withSorterWeightFactoryClass(
            Class<? extends SelectionSorterWeightFactory> sorterWeightFactoryClass) {
        this.sorterWeightFactoryClass = sorterWeightFactoryClass;
        return (Config_) this;
    }

    public Config_ withSorterOrder(SelectionSorterOrder sorterOrder) {
        this.sorterOrder = sorterOrder;
        return (Config_) this;
    }

    public Config_ withSorterClass(Class<? extends SelectionSorter> sorterClass) {
        this.sorterClass = sorterClass;
        return (Config_) this;
    }

    public Config_ withProbabilityWeightFactoryClass(
            Class<? extends SelectionProbabilityWeightFactory> probabilityWeightFactoryClass) {
        this.probabilityWeightFactoryClass = probabilityWeightFactoryClass;
        return (Config_) this;
    }

    public Config_ withSelectedCountLimit(Long selectedCountLimit) {
        this.selectedCountLimit = selectedCountLimit;
        return (Config_) this;
    }

    public Config_ withFixedProbabilityWeight(Double fixedProbabilityWeight) {
        this.fixedProbabilityWeight = fixedProbabilityWeight;
        return (Config_) this;
    }

    /**
     * Gather a list of all descendant {@link MoveSelectorConfig}s
     * except for {@link UnionMoveSelectorConfig} and {@link CartesianProductMoveSelectorConfig}.
     *
     * @param leafMoveSelectorConfigList not null
     */
    public void extractLeafMoveSelectorConfigsIntoList(List<MoveSelectorConfig> leafMoveSelectorConfigList) {
        leafMoveSelectorConfigList.add(this);
    }

    @Override
    public Config_ inherit(Config_ inheritedConfig) {
        inheritCommon(inheritedConfig);
        return (Config_) this;
    }

    /**
     * Does not inherit subclass properties because this class and {@code foldedConfig} can be of a different type.
     *
     * @param foldedConfig never null
     */
    public void inheritFolded(MoveSelectorConfig<?> foldedConfig) {
        inheritCommon(foldedConfig);
    }

    protected void visitCommonReferencedClasses(Consumer<Class<?>> classVisitor) {
        classVisitor.accept(filterClass);
        classVisitor.accept(sorterComparatorClass);
        classVisitor.accept(sorterWeightFactoryClass);
        classVisitor.accept(sorterClass);
        classVisitor.accept(probabilityWeightFactoryClass);
    }

    private void inheritCommon(MoveSelectorConfig<?> inheritedConfig) {
        cacheType = ConfigUtils.inheritOverwritableProperty(cacheType, inheritedConfig.getCacheType());
        selectionOrder = ConfigUtils.inheritOverwritableProperty(selectionOrder, inheritedConfig.getSelectionOrder());
        filterClass = ConfigUtils.inheritOverwritableProperty(filterClass, inheritedConfig.getFilterClass());
        sorterComparatorClass = ConfigUtils.inheritOverwritableProperty(
                sorterComparatorClass, inheritedConfig.getSorterComparatorClass());
        sorterWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                sorterWeightFactoryClass, inheritedConfig.getSorterWeightFactoryClass());
        sorterOrder = ConfigUtils.inheritOverwritableProperty(
                sorterOrder, inheritedConfig.getSorterOrder());
        sorterClass = ConfigUtils.inheritOverwritableProperty(
                sorterClass, inheritedConfig.getSorterClass());
        probabilityWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                probabilityWeightFactoryClass, inheritedConfig.getProbabilityWeightFactoryClass());
        selectedCountLimit = ConfigUtils.inheritOverwritableProperty(
                selectedCountLimit, inheritedConfig.getSelectedCountLimit());

        fixedProbabilityWeight = ConfigUtils.inheritOverwritableProperty(
                fixedProbabilityWeight, inheritedConfig.getFixedProbabilityWeight());
    }

}
