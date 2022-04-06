/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Makes up for the absence of LinkedIdentityHashMap.
 * It only adds the object if the instance wasn't added already.
 * Yet it keeps the original insertion order.
 *
 * @param <Solution_>
 */
public final class ActiveStreamSupport<Solution_> implements Set<BavetAbstractConstraintStream<Solution_>> {

    // For lookup performance.
    private final Map<BavetAbstractConstraintStream<Solution_>, Boolean> activeStreamsMap = new IdentityHashMap<>();
    // For iteration order.
    private final List<BavetAbstractConstraintStream<Solution_>> activeStreamsList = new ArrayList<>();

    @Override
    public int size() {
        return activeStreamsList.size();
    }

    @Override
    public boolean isEmpty() {
        return activeStreamsList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return activeStreamsMap.containsKey(o);
    }

    @Override
    public Iterator<BavetAbstractConstraintStream<Solution_>> iterator() {
        return activeStreamsList.iterator();
    }

    @Override
    public Object[] toArray() {
        return activeStreamsList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return activeStreamsList.toArray(a);
    }

    @Override
    public boolean add(BavetAbstractConstraintStream<Solution_> activeStream) {
        if (!contains(activeStream)) {
            activeStreamsMap.put(activeStream, true);
            activeStreamsList.add(activeStream);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            activeStreamsMap.remove(o);
            return activeStreamsList.remove(o);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends BavetAbstractConstraintStream<Solution_>> c) {
        boolean added = false;
        for (BavetAbstractConstraintStream<Solution_> stream : c) {
            added = add(stream) || added;
        }
        return added;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean removed = false;
        for (BavetAbstractConstraintStream<Solution_> stream : activeStreamsList) {
            if (c.contains(stream)) {
                removed = remove(c);
            }
        }
        return removed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object o : c) {
            removed = remove(o) || removed;
        }
        return removed;
    }

    @Override
    public void clear() {
        activeStreamsList.clear();
    }
}
