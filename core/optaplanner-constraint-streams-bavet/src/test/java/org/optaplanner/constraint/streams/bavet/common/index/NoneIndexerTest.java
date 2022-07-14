package org.optaplanner.constraint.streams.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;

class NoneIndexerTest extends AbstractIndexerTest {

    @Test
    void getEmpty() {
        Indexer<UniTupleImpl<String>, String> indexer = new NoneIndexer<>();
        assertThat(getTupleMap(indexer)).isEmpty();
    }

    @Test
    void putTwice() {
        Indexer<UniTupleImpl<String>, String> indexer = new NoneIndexer<>();
        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(NoneIndexProperties.INSTANCE, annTuple, "Ann value");
        assertThatThrownBy(() -> indexer.put(NoneIndexProperties.INSTANCE, annTuple, "Ann value"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removeTwice() {
        Indexer<UniTupleImpl<String>, String> indexer = new NoneIndexer<>();
        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(NoneIndexProperties.INSTANCE, annTuple, "Ann value");

        UniTupleImpl<String> ednaTuple = newTuple("Edna-F-40");
        assertThatThrownBy(() -> indexer.remove(NoneIndexProperties.INSTANCE, ednaTuple))
                .isInstanceOf(IllegalStateException.class);
        assertThat(indexer.remove(NoneIndexProperties.INSTANCE, annTuple))
                .isEqualTo("Ann value");
        assertThatThrownBy(() -> indexer.remove(NoneIndexProperties.INSTANCE, annTuple))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void visit() {
        Indexer<UniTupleImpl<String>, String> indexer = new NoneIndexer<>();

        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(NoneIndexProperties.INSTANCE, annTuple, "Ann value");
        UniTupleImpl<String> bethTuple = newTuple("Beth-F-30");
        indexer.put(NoneIndexProperties.INSTANCE, bethTuple, "Beth value");

        assertThat(getTupleMap(indexer)).containsOnlyKeys(annTuple, bethTuple);
    }

    private static UniTupleImpl<String> newTuple(String factA) {
        return new UniTupleImpl<>(factA, 0);
    }

}
