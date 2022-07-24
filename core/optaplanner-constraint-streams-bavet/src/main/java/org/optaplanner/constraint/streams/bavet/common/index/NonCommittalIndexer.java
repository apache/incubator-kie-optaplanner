package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.optaplanner.constraint.streams.bavet.common.Tuple;

final class NonCommittalIndexer<Tuple_ extends Tuple, Value_> implements Indexer<Tuple_, Value_> {

    private static final class Operation<Tuple_ extends Tuple, Value_> {

        private final IndexProperties indexProperties;
        private final Tuple_ tuple;
        private final Value_ value;

        public Operation(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
            this.indexProperties = indexProperties;
            this.tuple = tuple;
            this.value = value;
        }

    }

    private final Indexer<Tuple_, Value_> downstreamIndexer;
    // Tuples do not have identity; no need to incur HashMap's overheads.
    private final Map<Tuple_, Operation<Tuple_, Value_>> uncommittedOperations = new LinkedHashMap<>();
    private int uncommittedOperationCount = 0;

    public NonCommittalIndexer(Indexer<Tuple_, Value_> downstreamIndexer) {
        this.downstreamIndexer = downstreamIndexer;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        if (uncommittedOperationCount > 0) {
            for (Operation<Tuple_, Value_> operation : uncommittedOperations.values()) {
                downstreamIndexer.put(operation.indexProperties, operation.tuple, operation.value);
            }
            uncommittedOperations.clear();
            uncommittedOperationCount = 0;
        }
        downstreamIndexer.visit(indexProperties, tupleValueVisitor);
    }

    @Override
    public Value_ get(IndexProperties indexProperties, Tuple_ tuple) {
        if (uncommittedOperationCount > 0) {
            Operation<Tuple_, Value_> uncommittedOperation = uncommittedOperations.get(tuple);
            if (uncommittedOperation != null) {
                return uncommittedOperation.value;
            }
        }
        return downstreamIndexer.get(indexProperties, tuple);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Operation<Tuple_, Value_> previous = uncommittedOperations.put(tuple,
                new Operation<>(indexProperties, tuple, Objects.requireNonNull(value)));
        if (previous != null) {
            throw new IllegalStateException();
        }
        uncommittedOperationCount += 1;
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        if (uncommittedOperationCount > 0) {
            Operation<Tuple_, Value_> uncommittedOperation = uncommittedOperations.remove(tuple);
            if (uncommittedOperation != null) {
                uncommittedOperationCount -= 1;
                return uncommittedOperation.value;
            }
        }
        return downstreamIndexer.remove(indexProperties, tuple);
    }

    @Override
    public boolean isEmpty() {
        return uncommittedOperationCount == 0 && downstreamIndexer.isEmpty();
    }

}