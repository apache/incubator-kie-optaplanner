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
import org.optaplanner.constraint.streams.bavet.common.index.overlapping.impl.IntervalTree;
import org.optaplanner.core.impl.score.stream.JoinerType;

final class OverlappingIndexer<Tuple_ extends Tuple, Value_ extends Comparable<Value_>> implements Indexer<Tuple_, Value_> {
    private final Function<IndexProperties, Value_> startIndexPropertyFunction;

    private final Function<IndexProperties, Value_> endIndexPropertyFunction;
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final IntervalTree<OverlappingItem<Tuple_, Value_>, Value_, ?> comparisonMap;

    public OverlappingIndexer(JoinerType comparisonJoinerType,
                             Function<IndexProperties, Value_> startIndexPropertyFunction,
                             Function<IndexProperties, Value_> endIndexPropertyFunction,
                             Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        comparisonMap = new IntervalTree<>(overlappingItem -> startIndexPropertyFunction.apply(overlappingItem.indexProperties),
                                           overlappingItem -> endIndexPropertyFunction.apply(overlappingItem.indexProperties),
                                           (a,b) -> null);
        this.startIndexPropertyFunction = Objects.requireNonNull(startIndexPropertyFunction);
        this.endIndexPropertyFunction = Objects.requireNonNull(endIndexPropertyFunction);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Objects.requireNonNull(value);
        comparisonMap.add(comparisonMap.getInterval(new OverlappingItem<>(indexProperties, tuple, value)));

        // TODO: Downstream indexer
        // downstreamIndexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        // TODO: Downstream indexer
        // Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(comparisonIndexProperty);
        // if (downstreamIndexer == null) {
        //     throw new IllegalStateException("Impossible state: the tuple (" + tuple
        //                                             + ") with indexProperties (" + indexProperties
        //                                             + ") doesn't exist in the indexer.");
        // }
        // Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        //if (downstreamIndexer.isEmpty()) {
            comparisonMap.remove(comparisonMap.getInterval(new OverlappingItem<>(indexProperties, tuple, null)));
        //}
        return null;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        comparisonMap.visit(comparisonMap.getInterval(new OverlappingItem<>(indexProperties, null, null)), overlappingItem -> {
            tupleValueVisitor.accept(overlappingItem.tuple, overlappingItem.value);
        });
    }

    @Override
    public boolean isEmpty() {
        return comparisonMap.isEmpty();
    }

    private final static class OverlappingItem<Tuple_ extends Tuple, Value_ extends Comparable<Value_>> {
        final IndexProperties indexProperties;
        final Tuple_ tuple;
        final Value_ value;

        public OverlappingItem(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
            this.indexProperties = indexProperties;
            this.tuple = tuple;
            this.value = value;
        }
    }

}
