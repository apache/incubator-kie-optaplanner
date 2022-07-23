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
