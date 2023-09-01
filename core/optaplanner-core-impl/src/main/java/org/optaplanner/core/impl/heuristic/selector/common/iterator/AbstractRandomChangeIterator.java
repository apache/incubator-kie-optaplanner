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

package org.optaplanner.core.impl.heuristic.selector.common.iterator;

import java.util.Iterator;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public abstract class AbstractRandomChangeIterator<Solution_, Move_ extends Move<Solution_>>
        extends UpcomingSelectionIterator<Move_> {

    private final EntitySelector<Solution_> entitySelector;
    private final ValueSelector<Solution_> valueSelector;

    private Iterator<Object> entityIterator;

    public AbstractRandomChangeIterator(EntitySelector<Solution_> entitySelector,
            ValueSelector<Solution_> valueSelector) {
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        entityIterator = entitySelector.iterator();
        // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
    }

    @Override
    protected Move_ createUpcomingSelection() {
        // Ideally, this code should have read:
        //     Object entity = entityIterator.next();
        //     Iterator<Object> valueIterator = valueSelector.iterator(entity);
        //     Object toValue = valueIterator.next();
        // But empty selectors and ending selectors (such as non-random or shuffled) make it more complex
        if (!entityIterator.hasNext()) {
            entityIterator = entitySelector.iterator();
            if (!entityIterator.hasNext()) {
                return noUpcomingSelection();
            }
        }
        Object entity = entityIterator.next();

        Iterator<Object> valueIterator = valueSelector.iterator(entity);
        int entityIteratorCreationCount = 0;
        // This loop is mostly only relevant when the entityIterator or valueIterator is non-random or shuffled
        while (!valueIterator.hasNext()) {
            // Try the next entity
            if (!entityIterator.hasNext()) {
                entityIterator = entitySelector.iterator();
                entityIteratorCreationCount++;
                if (entityIteratorCreationCount >= 2) {
                    // All entity-value combinations have been tried (some even more than once)
                    return noUpcomingSelection();
                }
            }
            entity = entityIterator.next();
            valueIterator = valueSelector.iterator(entity);
        }
        Object toValue = valueIterator.next();
        return newChangeSelection(entity, toValue);
    }

    protected abstract Move_ newChangeSelection(Object entity, Object toValue);

}
