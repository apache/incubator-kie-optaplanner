package org.optaplanner.constraint.streams.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;
import org.optaplanner.constraint.streams.common.bi.DefaultBiJoiner;
import org.optaplanner.core.api.score.stream.Joiners;

class EqualsIndexerTest extends AbstractIndexerTest {

    private final DefaultBiJoiner<Person, Person> joiner =
            (DefaultBiJoiner<Person, Person>) Joiners.equal((Person p) -> p.gender)
                    .and(Joiners.equal((Person p) -> p.age));

    @Test
    void getEmpty() {
        Indexer<UniTupleImpl<String>, String> indexer = new IndexerFactory(joiner).buildIndexer(true);
        assertThat(getTupleMap(indexer, "F", 40)).isEmpty();
    }

    @Test
    void putTwice() {
        Indexer<UniTupleImpl<String>, String> indexer = new IndexerFactory(joiner).buildIndexer(true);
        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(new ManyIndexProperties("F", 40), annTuple, "Ann value");
        assertThatThrownBy(() -> indexer.put(new ManyIndexProperties("F", 40), annTuple, "Ann value"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removeTwice() {
        Indexer<UniTupleImpl<String>, String> indexer = new IndexerFactory(joiner).buildIndexer(true);
        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(new ManyIndexProperties("F", 40), annTuple, "Ann value");

        UniTupleImpl<String> ednaTuple = newTuple("Edna-F-40");
        assertThatThrownBy(() -> indexer.remove(new ManyIndexProperties("F", 40), ednaTuple))
                .isInstanceOf(IllegalStateException.class);
        assertThat(indexer.remove(new ManyIndexProperties("F", 40), annTuple))
                .isEqualTo("Ann value");
        assertThatThrownBy(() -> indexer.remove(new ManyIndexProperties("F", 40), annTuple))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void visit() {
        Indexer<UniTupleImpl<String>, String> indexer = new IndexerFactory(joiner).buildIndexer(true);

        UniTupleImpl<String> annTuple = newTuple("Ann-F-40");
        indexer.put(new ManyIndexProperties("F", 40), annTuple, "Ann value");
        UniTupleImpl<String> bethTuple = newTuple("Beth-F-30");
        indexer.put(new ManyIndexProperties("F", 30), bethTuple, "Beth value");
        indexer.put(new ManyIndexProperties("M", 40), newTuple("Carl-M-40"), "Carl value");
        indexer.put(new ManyIndexProperties("M", 30), newTuple("Dan-M-30"), "Dan value");
        UniTupleImpl<String> ednaTuple = newTuple("Edna-F-40");
        indexer.put(new ManyIndexProperties("F", 40), ednaTuple, "Edna value");

        assertThat(getTupleMap(indexer, "F", 40)).containsOnlyKeys(annTuple, ednaTuple);
        assertThat(getTupleMap(indexer, "F", 30)).containsOnlyKeys(bethTuple);
        assertThat(getTupleMap(indexer, "F", 20)).isEmpty();
    }

    private static UniTupleImpl<String> newTuple(String factA) {
        return new UniTupleImpl<>(factA, 0);
    }

}
