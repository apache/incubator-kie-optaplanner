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

import java.util.Collections;
import java.util.Iterator;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class OriginalListSwapIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final SingletonInverseVariableSupply inverseVariableSupply;
    private final IndexVariableSupply indexVariableSupply;
    private final Iterator<Object> leftValueIterator;
    private final EntityIndependentValueSelector<Solution_> rightValueSelector;
    private Iterator<Object> rightValueIterator;

    private Object upcomingLeftEntity;
    private Integer upcomingLeftIndex;

    public OriginalListSwapIterator(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply,
            EntityIndependentValueSelector<Solution_> leftValueSelector,
            EntityIndependentValueSelector<Solution_> rightValueSelector) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.indexVariableSupply = indexVariableSupply;
        this.leftValueIterator = leftValueSelector.iterator();
        this.rightValueIterator = Collections.emptyIterator();
        this.rightValueSelector = rightValueSelector;
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        while (!rightValueIterator.hasNext()) {
            if (!leftValueIterator.hasNext()) {
                return noUpcomingSelection();
            }
            Object upcomingLeftValue = leftValueIterator.next();
            upcomingLeftEntity = inverseVariableSupply.getInverseSingleton(upcomingLeftValue);
            upcomingLeftIndex = indexVariableSupply.getIndex(upcomingLeftValue);
            rightValueIterator = rightValueSelector.iterator();
        }

        Object upcomingRightValue = rightValueIterator.next();

        return new ListSwapMove<>(
                listVariableDescriptor,
                upcomingLeftEntity,
                upcomingLeftIndex,
                inverseVariableSupply.getInverseSingleton(upcomingRightValue),
                indexVariableSupply.getIndex(upcomingRightValue));
    }
}
