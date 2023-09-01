/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.constraint.streams.bavet.uni.UniTupleImpl;

class NoneIndexerTest extends AbstractIndexerTest {

    @Test
    void isEmpty() {
        Indexer<UniTuple<String>> indexer = new NoneIndexer<>();
        assertSoftly(softly -> {
            softly.assertThat(getTuples(indexer)).isEmpty();
            softly.assertThat(indexer.isEmpty()).isTrue();
        });
    }

    @Test
    void put() {
        Indexer<UniTuple<String>> indexer = new NoneIndexer<>();
        UniTuple<String> annTuple = newTuple("Ann-F-40");
        assertThat(indexer.size(NoneIndexProperties.INSTANCE)).isEqualTo(0);
        indexer.put(NoneIndexProperties.INSTANCE, annTuple);
        assertThat(indexer.size(NoneIndexProperties.INSTANCE)).isEqualTo(1);
        assertSoftly(softly -> {
            softly.assertThat(indexer.isEmpty()).isFalse();
            softly.assertThat(getTuples(indexer)).containsExactly(annTuple);
        });
    }

    @Test
    void removeTwice() {
        Indexer<UniTuple<String>> indexer = new NoneIndexer<>();
        UniTuple<String> annTuple = newTuple("Ann-F-40");
        TupleListEntry<UniTuple<String>> annEntry = indexer.put(NoneIndexProperties.INSTANCE, annTuple);
        assertSoftly(softly -> {
            softly.assertThat(indexer.isEmpty()).isFalse();
            softly.assertThat(getTuples(indexer)).containsExactly(annTuple);
        });

        indexer.remove(NoneIndexProperties.INSTANCE, annEntry);
        assertSoftly(softly -> {
            softly.assertThat(indexer.isEmpty()).isTrue();
            softly.assertThat(getTuples(indexer)).isEmpty();
        });
        assertThatThrownBy(() -> indexer.remove(NoneIndexProperties.INSTANCE, annEntry))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void visit() {
        Indexer<UniTuple<String>> indexer = new NoneIndexer<>();

        UniTuple<String> annTuple = newTuple("Ann-F-40");
        indexer.put(NoneIndexProperties.INSTANCE, annTuple);
        UniTuple<String> bethTuple = newTuple("Beth-F-30");
        indexer.put(NoneIndexProperties.INSTANCE, bethTuple);

        assertThat(getTuples(indexer)).containsOnly(annTuple, bethTuple);
    }

    private static UniTuple<String> newTuple(String factA) {
        return new UniTupleImpl<>(factA, 0);
    }

}
