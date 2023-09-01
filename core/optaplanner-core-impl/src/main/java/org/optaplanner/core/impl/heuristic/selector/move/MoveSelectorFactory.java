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

package org.optaplanner.core.impl.heuristic.selector.move;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveIteratorFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveListFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.KOptMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.kopt.KOptListMoveSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.composite.CartesianProductMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.composite.UnionMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactoryFactory;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactoryFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.KOptMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ListChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ListSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.SubListChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.SubListSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt.KOptListMoveSelectorFactory;

public interface MoveSelectorFactory<Solution_> {

    static <Solution_> MoveSelectorFactory<Solution_> create(MoveSelectorConfig<?> moveSelectorConfig) {
        if (ChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new ChangeMoveSelectorFactory<>((ChangeMoveSelectorConfig) moveSelectorConfig);
        } else if (SwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SwapMoveSelectorFactory<>((SwapMoveSelectorConfig) moveSelectorConfig);
        } else if (ListChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new ListChangeMoveSelectorFactory<>((ListChangeMoveSelectorConfig) moveSelectorConfig);
        } else if (ListSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new ListSwapMoveSelectorFactory<>((ListSwapMoveSelectorConfig) moveSelectorConfig);
        } else if (PillarChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new PillarChangeMoveSelectorFactory<>((PillarChangeMoveSelectorConfig) moveSelectorConfig);
        } else if (PillarSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new PillarSwapMoveSelectorFactory<>((PillarSwapMoveSelectorConfig) moveSelectorConfig);
        } else if (UnionMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new UnionMoveSelectorFactory<>((UnionMoveSelectorConfig) moveSelectorConfig);
        } else if (CartesianProductMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new CartesianProductMoveSelectorFactory<>((CartesianProductMoveSelectorConfig) moveSelectorConfig);
        } else if (SubListChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubListChangeMoveSelectorFactory<>((SubListChangeMoveSelectorConfig) moveSelectorConfig);
        } else if (SubListSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubListSwapMoveSelectorFactory<>((SubListSwapMoveSelectorConfig) moveSelectorConfig);
        } else if (SubChainChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubChainChangeMoveSelectorFactory<>((SubChainChangeMoveSelectorConfig) moveSelectorConfig);
        } else if (SubChainSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubChainSwapMoveSelectorFactory<>((SubChainSwapMoveSelectorConfig) moveSelectorConfig);
        } else if (TailChainSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new TailChainSwapMoveSelectorFactory<>((TailChainSwapMoveSelectorConfig) moveSelectorConfig);
        } else if (MoveIteratorFactoryConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new MoveIteratorFactoryFactory<>((MoveIteratorFactoryConfig) moveSelectorConfig);
        } else if (MoveListFactoryConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new MoveListFactoryFactory<>((MoveListFactoryConfig) moveSelectorConfig);
        } else if (KOptMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new KOptMoveSelectorFactory<>((KOptMoveSelectorConfig) moveSelectorConfig);
        } else if (KOptListMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new KOptListMoveSelectorFactory<>((KOptListMoveSelectorConfig) moveSelectorConfig);
        } else {
            throw new IllegalArgumentException(String.format("Unknown %s type: (%s).",
                    MoveSelectorConfig.class.getSimpleName(), moveSelectorConfig.getClass().getName()));
        }
    }

    /**
     * Builds {@link MoveSelector} from the {@link MoveSelectorConfig} and provided parameters.
     *
     * @param configPolicy never null
     * @param minimumCacheType never null, If caching is used (different from {@link SelectionCacheType#JUST_IN_TIME}),
     *        then it should be at least this {@link SelectionCacheType} because an ancestor already uses such caching
     *        and less would be pointless.
     * @param inheritedSelectionOrder never null
     * @return never null
     */
    MoveSelector<Solution_> buildMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder);
}
