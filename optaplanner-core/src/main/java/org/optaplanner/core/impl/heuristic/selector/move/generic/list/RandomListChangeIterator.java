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

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class RandomListChangeIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final Random workingRandom;

    private final NavigableMap<Integer, Object> sourceEntityByIndexNavigableMap;
    private final int sourceIndexRange;
    private final NavigableMap<Integer, Object> destinationEntityByIndexNavigableMap;
    private final int destinationIndexRange;

    public RandomListChangeIterator(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            List<Object> workingEntityList,
            Random workingRandom) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.workingRandom = workingRandom;

        sourceEntityByIndexNavigableMap = new TreeMap<>();
        destinationEntityByIndexNavigableMap = new TreeMap<>();
        int cumulativeListSize = 0;
        int cumulativeDestinationListSize = 0;
        for (Object entity : workingEntityList) {
            sourceEntityByIndexNavigableMap.put(cumulativeListSize, entity);
            cumulativeListSize += listVariableDescriptor.getListSize(entity);
            destinationEntityByIndexNavigableMap.put(cumulativeDestinationListSize, entity);
            cumulativeDestinationListSize += (listVariableDescriptor.getListSize(entity) + 1);
        }
        this.sourceIndexRange = cumulativeListSize;
        this.destinationIndexRange = cumulativeDestinationListSize;
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        Pair<Object, Integer> source =
                unfoldGlobalIndexIntoSourceEntityAndListIndex(workingRandom.nextInt(sourceIndexRange));
        Pair<Object, Integer> destination =
                unfoldGlobalIndexIntoDestinationEntityAndListIndex(workingRandom.nextInt(destinationIndexRange));

        return new ListChangeMove<>(
                source.getKey(),
                source.getValue(),
                destination.getKey(),
                destination.getValue(),
                listVariableDescriptor);
    }

    Pair<Object, Integer> unfoldGlobalIndexIntoSourceEntityAndListIndex(int index) {
        return unfoldGlobalIndexIntoEntityAndListIndex(sourceEntityByIndexNavigableMap, index);
    }

    Pair<Object, Integer> unfoldGlobalIndexIntoDestinationEntityAndListIndex(int index) {
        return unfoldGlobalIndexIntoEntityAndListIndex(destinationEntityByIndexNavigableMap, index);
    }

    private Pair<Object, Integer> unfoldGlobalIndexIntoEntityAndListIndex(NavigableMap<Integer, Object> map, int index) {
        Map.Entry<Integer, Object> entry = map.floorEntry(index);
        return Pair.of(entry.getValue(), index - entry.getKey());
    }
}
