package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.core.impl.util.FieldBasedScalingMap;

final class NoneIndexer<Tuple_ extends Tuple, Value_> implements Indexer<Tuple_, Value_> {

    private final Map<Tuple_, Value_> map = new FieldBasedScalingMap<>(LinkedHashMap::new);

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Value_ old = map.put(tuple, value);
        if (old != null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") was already added in the indexer.");
        }
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        Value_ value = map.remove(tuple);
        if (value == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        return value;
    }

    @Override
    public Value_ get(IndexProperties indexProperties, Tuple_ tuple) {
        Value_ value = map.get(tuple);
        if (value == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        return value;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        map.forEach(tupleValueVisitor);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    private final TupleList<Tuple_> tupleList = new TupleList<>();

    @Override
    public TupleListEntry<Tuple_> putGGG(IndexProperties indexProperties, Tuple_ tuple) {
        return tupleList.add(tuple);
    }

    @Override
    public void removeGGG(IndexProperties indexProperties, TupleListEntry<Tuple_> entry) {
        entry.remove();
    }

    @Override
    public void visitGGG(IndexProperties indexProperties, Consumer<TupleListEntry<Tuple_>> entryVisitor) {
        for (TupleListEntry<Tuple_> entry = tupleList.first(); entry != null; entry = entry.next()) {
            entryVisitor.accept(entry);
        }
    }

    @Override
    public boolean isEmptyGGG() {
        return tupleList.size() == 0;
    }

    @Override
    public String toString() {
        return "size = " + tupleList.size();
    }

}
