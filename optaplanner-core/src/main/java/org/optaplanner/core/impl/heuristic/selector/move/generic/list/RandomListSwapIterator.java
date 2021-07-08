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
public class RandomListSwapIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final int indexRange;
    private final Random workingRandom;
    private final NavigableMap<Integer, Object> entityByIndexNavigableMap;

    public RandomListSwapIterator(
            ListVariableDescriptor<Solution_> variableDescriptor,
            List<Object> workingEntityList,
            Random workingRandom) {
        this.variableDescriptor = variableDescriptor;
        this.workingRandom = workingRandom;

        entityByIndexNavigableMap = new TreeMap<>();
        int cumulativeListSize = 0;
        for (Object entity : workingEntityList) {
            entityByIndexNavigableMap.put(cumulativeListSize, entity);
            cumulativeListSize += variableDescriptor.getListSize(entity);
        }
        this.indexRange = cumulativeListSize;
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        if (indexRange == 0) {
            return noUpcomingSelection();
        }
        Pair<Object, Integer> left = unfoldGlobalIndexIntoEntityAndListIndex(workingRandom.nextInt(indexRange));
        Pair<Object, Integer> right = unfoldGlobalIndexIntoEntityAndListIndex(workingRandom.nextInt(indexRange));
        return new ListSwapMove<>(
                left.getKey(),
                left.getValue(),
                right.getKey(),
                right.getValue(),
                variableDescriptor);
    }

    Pair<Object, Integer> unfoldGlobalIndexIntoEntityAndListIndex(int index) {
        Map.Entry<Integer, Object> entry = entityByIndexNavigableMap.floorEntry(index);
        return Pair.of(entry.getValue(), index - entry.getKey());
    }
}
