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

package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Uses {@link LinkedHashMap} as tuple storage unless it is the first and only tuple,
 * in which case it uses {@link Collections#singletonMap(Object, Object)}.
 *
 * This helps avoid the overhead of creating and accessing a hash map if we only have 1 tuple in the index.
 */
final class NoneIndexerStorage<Key_, Value_> {

    private Map<Key_, Value_> map;
    private int size = 0;

    public Value_ put(Key_ tuple, Value_ value) {
        if (map == null) {
            map = Collections.singletonMap(tuple, value);
            size = 1;
            return null;
        } else if (isSingletonMap()) {
            Map.Entry<Key_, Value_> entry = map.entrySet().iterator().next();
            map = new LinkedHashMap<>();
            map.put(entry.getKey(), entry.getValue());
        }
        size += 1;
        return map.put(tuple, value);
    }

    private boolean isSingletonMap() {
        return size == 1 && !(map instanceof LinkedHashMap);
    }

    public Value_ remove(Key_ tuple) {
        if (isSingletonMap()) {
            Value_ value = map.get(tuple);
            map = null;
            size = 0;
            return value;
        }
        Value_ value = map.remove(tuple);
        size -= 1;
        return value;
    }

    public Value_ get(Key_ tuple) {
        return map.get(tuple);
    }

    public void visit(BiConsumer<Key_, Value_> tupleValueVisitor) {
        if (size == 0) {
            return;
        }
        map.forEach(tupleValueVisitor);
    }

    public boolean isEmpty() {
        return size == 0;
    }

}
