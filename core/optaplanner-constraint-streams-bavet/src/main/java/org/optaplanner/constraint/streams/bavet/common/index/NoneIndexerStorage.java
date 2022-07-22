package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

final class NoneIndexerStorage<Key_, Value_> {

    private Map<Key_, Value_> map;
    private int size = 0;

    public Value_ put(Key_ tuple, Value_ value) {
        if (size == 0) {
            map = Collections.singletonMap(tuple, value);
            size = 1;
            return null;
        } else if (size == 1) {
            Map.Entry<Key_, Value_> entry = map.entrySet().iterator().next();
            map = new LinkedHashMap<>();
            map.put(entry.getKey(), entry.getValue());
        }
        size += 1;
        return map.put(tuple, value);
    }

    public Value_ remove(Key_ tuple) {
        if (size == 1) {
            Value_ value = map.get(tuple);
            map = null;
            size = 0;
            return value;
        }
        Value_ value = map.remove(tuple);
        size -= 1;
        if (size == 1) {
            Map.Entry<Key_, Value_> entry = map.entrySet().iterator().next();
            map = Collections.singletonMap(entry.getKey(), entry.getValue());
        }
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
