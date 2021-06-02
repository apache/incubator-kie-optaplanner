/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;

public class ListSwapMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final EntitySelector<Solution_> leftEntitySelector;
    private final EntitySelector<Solution_> rightEntitySelector;

    public ListSwapMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntitySelector<Solution_> leftEntitySelector,
            EntitySelector<Solution_> rightEntitySelector) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.leftEntitySelector = leftEntitySelector;
        this.rightEntitySelector = rightEntitySelector;
        phaseLifecycleSupport.addEventListener(leftEntitySelector);
        if (leftEntitySelector != rightEntitySelector) {
            phaseLifecycleSupport.addEventListener(rightEntitySelector);
        }
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new NaiveListSwapIterator<>(leftEntitySelector, rightEntitySelector, listVariableDescriptor);
    }

    @Override
    public boolean isCountable() {
        return false;
    }

    @Override
    public boolean isNeverEnding() {
        return false;
    }
}
