package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.optaplanner.constraint.streams.bavet.common.Tuple;

/**
 * Some additions to the index will be reverted without ever iterating over the downstream indexer contents.
 * If we buffer all additions, we can safely remove any of them from the buffer
 * before {@link #visit(IndexProperties, BiConsumer)} is called without ever reaching the downstream indexer.
 * When {@link #visit(IndexProperties, BiConsumer)} is called, we empty the rest into the downstream indexer,
 * finally paying the indexing price on this smaller set of tuples.
 *
 * <p>
 * As range indexing is expensive, avoiding it represents a major performance gain.
 * It is therefore recommended to place this indexer immediately upstream from the top-most {@link ComparisonIndexer}
 * of any indexer chain which contains a {@link ComparisonIndexer}.
 * All subsequent {@link ComparisonIndexer}s in that chain will benefit too, as the buffer is applied once at the top.
 * {@link EqualsIndexer} will not benefit from this, as that just puts one {@link java.util.HashMap} in front of another.
 *
 * @param <Tuple_>
 * @param <Value_>
 */
final class BufferingIndexer<Tuple_ extends Tuple, Value_> implements Indexer<Tuple_, Value_> {

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
    /*
     * Tuples do not have identity; no need to incur HashMap's overheads.
     * We also do not need consistent iteration order, as that will be guaranteed by the ComparisonIndexer downstream.
     * Therefore, IdentityHashMap is the fastest possible option.
     */
    private final Map<Tuple_, Operation<Tuple_, Value_>> bufferedOperationsMap = new IdentityHashMap<>();
    private int bufferedOperationCount = 0;

    public BufferingIndexer(Indexer<Tuple_, Value_> downstreamIndexer) {
        this.downstreamIndexer = downstreamIndexer;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        if (bufferedOperationCount > 0) {
            for (Operation<Tuple_, Value_> operation : bufferedOperationsMap.values()) {
                downstreamIndexer.put(operation.indexProperties, operation.tuple, operation.value);
            }
            bufferedOperationsMap.clear();
            bufferedOperationCount = 0;
        }
        downstreamIndexer.visit(indexProperties, tupleValueVisitor);
    }

    @Override
    public Value_ get(IndexProperties indexProperties, Tuple_ tuple) {
        if (bufferedOperationCount > 0) {
            Operation<Tuple_, Value_> uncommittedOperation = bufferedOperationsMap.get(tuple);
            if (uncommittedOperation != null) {
                return uncommittedOperation.value;
            }
        }
        return downstreamIndexer.get(indexProperties, tuple);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Operation<Tuple_, Value_> previous = bufferedOperationsMap.put(tuple,
                new Operation<>(indexProperties, tuple, Objects.requireNonNull(value)));
        if (previous != null) {
            throw new IllegalStateException();
        }
        bufferedOperationCount += 1;
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        if (bufferedOperationCount > 0) {
            Operation<Tuple_, Value_> uncommittedOperation = bufferedOperationsMap.remove(tuple);
            if (uncommittedOperation != null) {
                bufferedOperationCount -= 1;
                return uncommittedOperation.value;
            }
        }
        return downstreamIndexer.remove(indexProperties, tuple);
    }

    @Override
    public boolean isEmpty() {
        return bufferedOperationCount == 0 && downstreamIndexer.isEmpty();
    }

}