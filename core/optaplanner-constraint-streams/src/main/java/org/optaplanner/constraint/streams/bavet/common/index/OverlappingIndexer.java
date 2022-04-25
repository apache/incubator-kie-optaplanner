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

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.bavet.common.index.overlapping.impl.Interval;
import org.optaplanner.constraint.streams.bavet.common.index.overlapping.impl.IntervalTree;

final class OverlappingIndexer<Tuple_ extends Tuple, Value_> implements Indexer<Tuple_, Value_> {
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final IntervalTree<OverlappingItem<Tuple_, Value_>, ?, ?> intervalTree;

    public OverlappingIndexer(Function<IndexProperties, Value_> startIndexPropertyFunction,
                              Function<IndexProperties, Value_> endIndexPropertyFunction,
                              Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        intervalTree = new IntervalTree<>(overlappingItem -> (Comparable) startIndexPropertyFunction.apply(overlappingItem.indexProperties),
                                          overlappingItem -> (Comparable) endIndexPropertyFunction.apply(overlappingItem.indexProperties),
                                          (a,b) -> null);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Objects.requireNonNull(value);
        Interval<OverlappingItem<Tuple_, Value_>, ?> interval = intervalTree.computeIfAbsent(indexProperties, properties -> new OverlappingItem<>(properties, tuple, value, downstreamIndexerSupplier.get()));
        interval.getValue().indexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        Interval<OverlappingItem<Tuple_, Value_>, ?> interval = intervalTree.getIntervalByProperties(indexProperties);
        Indexer<Tuple_, Value_> downstreamIndexer = interval.getValue().indexer;
        if (downstreamIndexer == null) {
             throw new IllegalStateException("Impossible state: the tuple (" + tuple
                                                     + ") with indexProperties (" + indexProperties
                                                     + ") doesn't exist in the indexer.");
        }
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            intervalTree.remove(indexProperties, interval);
        }
        return null;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        intervalTree.visit(intervalTree.getInterval(new OverlappingItem<>(indexProperties, null, null, null)),
                           overlappingItem -> {
            tupleValueVisitor.accept(overlappingItem.tuple, overlappingItem.value);
            overlappingItem.indexer.visit(indexProperties, tupleValueVisitor);
        });
    }

    @Override
    public boolean isEmpty() {
        return intervalTree.isEmpty();
    }

    private final static class OverlappingItem<Tuple_ extends Tuple, Value_> {
        final IndexProperties indexProperties;
        final Tuple_ tuple;
        final Value_ value;

        final Indexer<Tuple_, Value_> indexer;

        public OverlappingItem(IndexProperties indexProperties, Tuple_ tuple, Value_ value, Indexer<Tuple_, Value_> indexer) {
            this.indexProperties = indexProperties;
            this.tuple = tuple;
            this.value = value;
            this.indexer = indexer;
        }
    }

}
