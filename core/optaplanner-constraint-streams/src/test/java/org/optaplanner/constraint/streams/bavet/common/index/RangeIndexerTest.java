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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.impl.score.stream.JoinerType;

class RangeIndexerTest extends AbstractIndexerTest {

    private static final LocalDateTime TODAY = LocalDateTime.now();
    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime TOMORROW = LocalDateTime.now().plusDays(1);

    private Indexer<UniTuple<String>, String> createGtLtIndexer() {
        return createIndexer(JoinerType.GREATER_THAN, JoinerType.LESS_THAN);
    }

    private Indexer<UniTuple<String>, String> createIndexer(JoinerType leftJoinerType, JoinerType rightJoinerType) {
        return new RangeIndexer<>(leftJoinerType, rightJoinerType,
                indexProperties -> indexProperties.getProperty(0), indexProperties -> indexProperties.getProperty(1),
                NoneIndexer::new);
    }

    @Test
    void getEmpty() {
        Indexer<UniTuple<String>, String> indexer = createGtLtIndexer();
        assertThat(getTupleMap(indexer, TODAY, TOMORROW)).isEmpty();
    }

    @Test
    void putTwice() {
        Indexer<UniTuple<String>, String> indexer = createGtLtIndexer();
        UniTuple<String> annTuple = newTuple("Ann-F-40");
        indexer.put(new ManyIndexProperties(TODAY, TOMORROW), annTuple, "Ann value");
        assertThatThrownBy(() -> indexer.put(new ManyIndexProperties(TODAY, TOMORROW), annTuple, "Ann value"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removeTwice() {
        Indexer<UniTuple<String>, String> indexer = createGtLtIndexer();
        UniTuple<String> annTuple = newTuple("Ann-F-40");
        indexer.put(new ManyIndexProperties(TODAY, TOMORROW), annTuple, "Ann value");

        UniTuple<String> ednaTuple = newTuple("Edna-F-40");
        assertThatThrownBy(() -> indexer.remove(new ManyIndexProperties(TODAY, TOMORROW), ednaTuple))
                .isInstanceOf(IllegalStateException.class);
        assertThat(indexer.remove(new ManyIndexProperties(TODAY, TOMORROW), annTuple))
                .isEqualTo("Ann value");
        assertThatThrownBy(() -> indexer.remove(new ManyIndexProperties(TODAY, TOMORROW), annTuple))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void visitLtGt() {
        Indexer<UniTuple<String>, String> indexer = createGtLtIndexer();

        UniTuple<String> annTuple = newTuple("Ann-YESTERDAY-TOMORROW");
        indexer.put(new ManyIndexProperties(YESTERDAY, TOMORROW), annTuple, "Ann value");
        UniTuple<String> bethTuple = newTuple("Beth-YESTERDAY-TODAY");
        indexer.put(new ManyIndexProperties(YESTERDAY, TODAY), bethTuple, "Beth value");
        UniTuple<String> carlTuple = newTuple("Carl-TODAY-TOMORROW");
        indexer.put(new ManyIndexProperties(TODAY, TOMORROW), carlTuple, "Carl value");
        // The following two tuples should be ignored, as nothing can be both > and < the same date.
        UniTuple<String> danTuple = newTuple("Dan-TODAY-TODAY");
        indexer.put(new ManyIndexProperties(TODAY, TODAY), danTuple, "Dan value");
        UniTuple<String> ednaTuple = newTuple("Edna-YESTERDAY-TOMORROW");
        indexer.put(new ManyIndexProperties(TOMORROW, TOMORROW), ednaTuple, "Edna value");

        LocalDateTime beginningOfTime = Instant.EPOCH.atOffset(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime muchLater = TOMORROW.plusDays(1);
        assertThat(getTupleMap(indexer, beginningOfTime, muchLater))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, TOMORROW))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, TODAY))
                .containsOnlyKeys(annTuple, bethTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, YESTERDAY))
                .isEmpty();
        assertThat(getTupleMap(indexer, YESTERDAY, muchLater))
                .containsOnlyKeys(carlTuple);
        assertThat(getTupleMap(indexer, TODAY, muchLater))
                .isEmpty();
        assertThat(getTupleMap(indexer, TOMORROW, muchLater))
                .isEmpty();
    }

    @Test
    void visitLeGe() {
        Indexer<UniTuple<String>, String> indexer =
                createIndexer(JoinerType.GREATER_THAN_OR_EQUAL, JoinerType.LESS_THAN_OR_EQUAL);

        UniTuple<String> annTuple = newTuple("Ann-YESTERDAY-TOMORROW");
        indexer.put(new ManyIndexProperties(YESTERDAY, TOMORROW), annTuple, "Ann value");
        UniTuple<String> bethTuple = newTuple("Beth-YESTERDAY-TODAY");
        indexer.put(new ManyIndexProperties(YESTERDAY, TODAY), bethTuple, "Beth value");
        UniTuple<String> carlTuple = newTuple("Carl-TODAY-TOMORROW");
        indexer.put(new ManyIndexProperties(TODAY, TOMORROW), carlTuple, "Carl value");
        UniTuple<String> danTuple = newTuple("Dan-TODAY-TODAY");
        indexer.put(new ManyIndexProperties(TODAY, TODAY), danTuple, "Dan value");
        UniTuple<String> ednaTuple = newTuple("Edna-YESTERDAY-TOMORROW");
        indexer.put(new ManyIndexProperties(TOMORROW, TOMORROW), ednaTuple, "Edna value");

        LocalDateTime beginningOfTime = Instant.EPOCH.atOffset(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime muchLater = TOMORROW.plusDays(1);
        assertThat(getTupleMap(indexer, beginningOfTime, muchLater))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple, danTuple, ednaTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, TOMORROW))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple, danTuple, ednaTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, TODAY))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple, danTuple);
        assertThat(getTupleMap(indexer, beginningOfTime, YESTERDAY))
                .containsOnlyKeys(annTuple, bethTuple);
        assertThat(getTupleMap(indexer, YESTERDAY, muchLater))
                .containsOnlyKeys(annTuple, bethTuple, carlTuple, danTuple, ednaTuple);
        assertThat(getTupleMap(indexer, TODAY, muchLater))
                .containsOnlyKeys(carlTuple, danTuple, ednaTuple);
        assertThat(getTupleMap(indexer, TOMORROW, muchLater))
                .containsOnlyKeys(ednaTuple);
    }

    private static UniTuple<String> newTuple(String factA) {
        return new UniTuple<>(factA, 0);
    }

}
