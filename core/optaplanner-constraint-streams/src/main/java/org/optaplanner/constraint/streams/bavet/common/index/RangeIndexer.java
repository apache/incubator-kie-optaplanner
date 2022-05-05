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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lodborg.intervaltree.Interval;
import com.lodborg.intervaltree.IntervalTree;
import org.apache.commons.math3.util.Pair;
import org.optaplanner.constraint.streams.bavet.common.Tuple;

final class RangeIndexer<Tuple_ extends Tuple, Value_, Key_ extends Comparable<Key_>> implements Indexer<Tuple_, Value_> {
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final IntervalTree<Key_> intervalTree;

    private final Map<Pair<Key_, Key_>, TupleInterval<Tuple_, Value_, Key_>> intervalMap;

    private final Function<IndexProperties, Key_> startComparisonIndexPropertyFunction;

    private final Function<IndexProperties, Key_> endComparisonIndexPropertyFunction;

    public RangeIndexer(Function<IndexProperties, Key_> startComparisonIndexPropertyFunction,
            Function<IndexProperties, Key_> endComparisonIndexPropertyFunction,
            Supplier<Indexer<Tuple_, Value_>> actualDownstreamIndexerSupplier) {
        intervalTree = new IntervalTree<>();
        intervalMap = new HashMap<>();
        this.startComparisonIndexPropertyFunction = startComparisonIndexPropertyFunction;
        this.endComparisonIndexPropertyFunction = endComparisonIndexPropertyFunction;
        this.downstreamIndexerSupplier = Objects.requireNonNull(actualDownstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Objects.requireNonNull(value);

        Key_ end = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ start = endComparisonIndexPropertyFunction.apply(indexProperties);

        TupleInterval<Tuple_, Value_, Key_> interval = intervalMap.computeIfAbsent(Pair.create(start, end),
                  startEnd -> new TupleInterval<>(start, end, tuple, value, downstreamIndexerSupplier.get()));

        if (start.compareTo(end) < 0) {
            intervalTree.add(interval);
        }
        interval.indexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        Key_ end = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ start = endComparisonIndexPropertyFunction.apply(indexProperties);

        final Pair<Key_, Key_> startEndPair = Pair.create(start, end);
        TupleInterval<Tuple_, Value_, Key_> interval = intervalMap.get(startEndPair);
        if (interval == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        Indexer<Tuple_, Value_> downstreamIndexer = interval.indexer;
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            if (start.compareTo(end) < 0) {
                intervalTree.remove(interval);
            }
            intervalMap.remove(startEndPair);
        }
        return value;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        Key_ start = startComparisonIndexPropertyFunction.apply(indexProperties);
        Key_ end = endComparisonIndexPropertyFunction.apply(indexProperties);

        intervalTree.query(new QueryInterval<>(start, end)).forEach(interval -> {
            ((TupleInterval<Tuple_, Value_, Key_>) interval).indexer.visit(indexProperties, tupleValueVisitor);
        });
    }

    @Override
    public boolean isEmpty() {
        return intervalTree.isEmpty();
    }

    private final static class QueryInterval<Key_ extends Comparable<Key_>> extends Interval<Key_> {

        public QueryInterval(Key_ start, Key_ end) {
            super(start, end, Bounded.OPEN);
        }

        @Override
        protected Interval<Key_> create() {
            return new QueryInterval<>(null, null);
        }

        @Override
        public Key_ getMidpoint() {
            return getStart();
        }
    }

    private final static class TupleInterval<Tuple_ extends Tuple, Value_, Key_ extends Comparable<Key_>> extends Interval<Key_> {
        final Tuple_ tuple;
        final Value_ value;

        final Indexer<Tuple_, Value_> indexer;

        public TupleInterval(Key_ start, Key_ end, Tuple_ tuple, Value_ value, Indexer<Tuple_, Value_> indexer) {
            super(start, end, Bounded.CLOSED_LEFT);
            this.tuple = tuple;
            this.value = value;
            this.indexer = indexer;
        }

        @Override
        protected Interval<Key_> create() {
            return new TupleInterval<>(null, null, null, null, null);
        }

        @Override
        public Key_ getMidpoint() {
            return getStart();
        }
    }

}
