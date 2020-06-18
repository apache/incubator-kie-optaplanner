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

package org.optaplanner.core.impl.heuristic.selector.common.iterator;

import java.util.Collections;
import java.util.Iterator;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public abstract class AbstractOriginalChangeIterator<S extends Move> extends UpcomingSelectionIterator<S> {

    private final ValueSelector valueSelector;

    private final Iterator<Object> entityIterator;
    private Iterator<Object> valueIterator;

    private Object upcomingEntity;

    public AbstractOriginalChangeIterator(EntitySelector entitySelector, ValueSelector valueSelector) {
        this.valueSelector = valueSelector;
        entityIterator = entitySelector.iterator();
        // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
        valueIterator = Collections.emptyIterator();
    }

    @Override
    protected S createUpcomingSelection() {
        while (!valueIterator.hasNext()) {
            if (!entityIterator.hasNext()) {
                return noUpcomingSelection();
            }
            upcomingEntity = entityIterator.next();
            valueIterator = valueSelector.iterator(upcomingEntity);
        }
        Object toValue = valueIterator.next();
        return newChangeSelection(upcomingEntity, toValue);
    }

    protected abstract S newChangeSelection(Object entity, Object toValue);

}
