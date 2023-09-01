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

package org.optaplanner.core.impl.heuristic.selector.entity.pillar;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.entity.pillar.PillarSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.entity.pillar.SubPillarConfigPolicy;
import org.optaplanner.core.config.heuristic.selector.move.generic.SubPillarType;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.solver.ClassInstanceCache;

public class PillarSelectorFactory<Solution_>
        extends AbstractSelectorFactory<Solution_, PillarSelectorConfig> {

    public static <Solution_> PillarSelectorFactory<Solution_> create(PillarSelectorConfig pillarSelectorConfig) {
        return new PillarSelectorFactory<>(pillarSelectorConfig);
    }

    public PillarSelectorFactory(PillarSelectorConfig pillarSelectorConfig) {
        super(pillarSelectorConfig);
    }

    /**
     * @param configPolicy never null
     * @param subPillarType if null, defaults to {@link SubPillarType#ALL} for backwards compatibility reasons.
     * @param subPillarSequenceComparatorClass if not null, will force entities in the pillar to come in this order
     * @param minimumCacheType never null, If caching is used (different from {@link SelectionCacheType#JUST_IN_TIME}),
     *        then it should be at least this {@link SelectionCacheType} because an ancestor already uses such caching
     *        and less would be pointless.
     * @param inheritedSelectionOrder never null
     * @param variableNameIncludeList sometimes null
     * @return never null
     */
    public PillarSelector<Solution_> buildPillarSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SubPillarType subPillarType, Class<? extends Comparator<Object>> subPillarSequenceComparatorClass,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder,
            List<String> variableNameIncludeList) {
        if (subPillarType != SubPillarType.SEQUENCE && subPillarSequenceComparatorClass != null) {
            throw new IllegalArgumentException("Subpillar type (" + subPillarType + ") on pillarSelectorConfig (" + config +
                    ") is not " + SubPillarType.SEQUENCE + ", yet subPillarSequenceComparatorClass (" +
                    subPillarSequenceComparatorClass + ") is provided.");
        }
        if (minimumCacheType.compareTo(SelectionCacheType.STEP) > 0) {
            throw new IllegalArgumentException("The pillarSelectorConfig (" + config
                    + ")'s minimumCacheType (" + minimumCacheType
                    + ") must not be higher than " + SelectionCacheType.STEP
                    + " because the pillars change every step.");
        }
        boolean subPillarEnabled = subPillarType != SubPillarType.NONE;
        // EntitySelector uses SelectionOrder.ORIGINAL because a DefaultPillarSelector STEP caches the values
        EntitySelectorConfig entitySelectorConfig =
                Objects.requireNonNullElseGet(config.getEntitySelectorConfig(), EntitySelectorConfig::new);
        EntitySelector<Solution_> entitySelector =
                EntitySelectorFactory.<Solution_> create(entitySelectorConfig)
                        .buildEntitySelector(configPolicy, minimumCacheType, SelectionOrder.ORIGINAL);
        List<GenuineVariableDescriptor<Solution_>> variableDescriptors =
                deduceVariableDescriptorList(entitySelector.getEntityDescriptor(), variableNameIncludeList);
        if (!subPillarEnabled
                && (config.getMinimumSubPillarSize() != null || config.getMaximumSubPillarSize() != null)) {
            throw new IllegalArgumentException("The pillarSelectorConfig (" + config
                    + ") must not disable subpillars while providing minimumSubPillarSize (" + config.getMinimumSubPillarSize()
                    + ") or maximumSubPillarSize (" + config.getMaximumSubPillarSize() + ").");
        }

        SubPillarConfigPolicy subPillarPolicy = subPillarEnabled
                ? configureSubPillars(subPillarType, subPillarSequenceComparatorClass, entitySelector,
                        config.getMinimumSubPillarSize(), config.getMaximumSubPillarSize(),
                        configPolicy.getClassInstanceCache())
                : SubPillarConfigPolicy.withoutSubpillars();
        return new DefaultPillarSelector<>(entitySelector, variableDescriptors,
                inheritedSelectionOrder.toRandomSelectionBoolean(), subPillarPolicy);
    }

    private SubPillarConfigPolicy configureSubPillars(SubPillarType pillarType,
            Class<? extends Comparator<Object>> pillarOrderComparatorClass, EntitySelector<Solution_> entitySelector,
            Integer minimumSubPillarSize, Integer maximumSubPillarSize, ClassInstanceCache instanceCache) {
        int actualMinimumSubPillarSize = Objects.requireNonNullElse(minimumSubPillarSize, 1);
        int actualMaximumSubPillarSize = Objects.requireNonNullElse(maximumSubPillarSize, Integer.MAX_VALUE);
        if (pillarType == null) { // for backwards compatibility reasons
            return SubPillarConfigPolicy.withSubpillars(actualMinimumSubPillarSize, actualMaximumSubPillarSize);
        }
        switch (pillarType) {
            case ALL:
                return SubPillarConfigPolicy.withSubpillars(actualMinimumSubPillarSize, actualMaximumSubPillarSize);
            case SEQUENCE:
                if (pillarOrderComparatorClass == null) {
                    Class<?> entityClass = entitySelector.getEntityDescriptor().getEntityClass();
                    boolean isComparable = Comparable.class.isAssignableFrom(entityClass);
                    if (!isComparable) {
                        throw new IllegalArgumentException("Pillar type (" + pillarType + ") on pillarSelectorConfig (" +
                                config + ") does not provide pillarOrderComparatorClass while the entity (" +
                                entityClass.getCanonicalName() + ") does not implement Comparable.");
                    }
                    return SubPillarConfigPolicy.sequential(actualMinimumSubPillarSize, actualMaximumSubPillarSize,
                            Comparator.naturalOrder());
                } else {
                    Comparator<Object> comparator = instanceCache.newInstance(config, "pillarOrderComparatorClass",
                            pillarOrderComparatorClass);
                    return SubPillarConfigPolicy.sequential(actualMinimumSubPillarSize, actualMaximumSubPillarSize, comparator);
                }
            default:
                throw new IllegalStateException("Subpillars cannot be enabled and disabled at the same time.");
        }
    }

}
