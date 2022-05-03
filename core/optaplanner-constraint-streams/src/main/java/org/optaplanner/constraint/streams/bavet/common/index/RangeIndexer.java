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

final class RangeIndexer<Tuple_ extends Tuple, Value_, Key_ extends Comparable<Key_>> implements Indexer<Tuple_, Value_> {
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final IntervalTree<TupleInterval<Tuple_, Value_, Key_>, Key_, ?> intervalTree;

    private final Function<IndexProperties, Key_> startComparisonIndexPropertyFunction;

    private final Function<IndexProperties, Key_> endComparisonIndexPropertyFunction;

    public RangeIndexer(Function<IndexProperties, Key_> startComparisonIndexPropertyFunction,
            Function<IndexProperties, Key_> endComparisonIndexPropertyFunction,
            Supplier<Indexer<Tuple_, Value_>> actualDownstreamIndexerSupplier) {
        intervalTree = new IntervalTree<>((a, b) -> null);
        this.startComparisonIndexPropertyFunction = startComparisonIndexPropertyFunction;
        this.endComparisonIndexPropertyFunction = endComparisonIndexPropertyFunction;
        this.downstreamIndexerSupplier = Objects.requireNonNull(actualDownstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Objects.requireNonNull(value);
        Key_ start = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ end = endComparisonIndexPropertyFunction.apply(indexProperties);

        Interval<TupleInterval<Tuple_, Value_, Key_>, ?> interval = intervalTree.computeIfAbsent(start, end,
                () -> new TupleInterval<>(start, end, tuple, value, downstreamIndexerSupplier.get()));
        interval.getValue().indexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        Key_ start = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ end = endComparisonIndexPropertyFunction.apply(indexProperties);

        Interval<TupleInterval<Tuple_, Value_, Key_>, ?> interval = intervalTree.getIntervalByRange(start, end);
        if (interval == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
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
        return value;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        Key_ start = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ end = endComparisonIndexPropertyFunction.apply(indexProperties);

        intervalTree.visit(intervalTree.getInterval(new TupleInterval<>(start, end, null, null, null),
                start, end),
                tupleInterval -> {
                    tupleInterval.indexer.visit(indexProperties, tupleValueVisitor);
                });
    }

    @Override
    public boolean isEmpty() {
        return intervalTree.isEmpty();
    }

    private final static class TupleInterval<Tuple_ extends Tuple, Value_, Key_ extends Comparable<Key_>> {
        final Key_ start;

        final Key_ end;
        final Tuple_ tuple;
        final Value_ value;

        final Indexer<Tuple_, Value_> indexer;

        public TupleInterval(Key_ start, Key_ end, Tuple_ tuple, Value_ value, Indexer<Tuple_, Value_> indexer) {
            this.start = start;
            this.end = end;
            this.tuple = tuple;
            this.value = value;
            this.indexer = indexer;
        }
    }

}
