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

package org.optaplanner.core.impl.heuristic.selector.move.composite;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;

public class CartesianProductMoveSelectorFactory<Solution_>
        extends AbstractCompositeMoveSelectorFactory<Solution_, CartesianProductMoveSelectorConfig> {

    public CartesianProductMoveSelectorFactory(CartesianProductMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    public MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        List<MoveSelector<Solution_>> moveSelectorList = buildInnerMoveSelectors(config.getMoveSelectorConfigList(),
                configPolicy, minimumCacheType, randomSelection);
        boolean ignoreEmptyChildIterators_ = defaultIfNull(config.getIgnoreEmptyChildIterators(), true);
        return new CartesianProductMoveSelector<>(moveSelectorList, ignoreEmptyChildIterators_, randomSelection);
    }
}
