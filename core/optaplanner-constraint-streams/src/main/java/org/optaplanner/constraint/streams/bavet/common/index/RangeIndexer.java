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

import static org.optaplanner.constraint.streams.bavet.common.index.ComparisonIndexer.getSubmapFunction;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.JoinerType;

final class RangeIndexer<IndexProperty_ extends Comparable<IndexProperty_>, Tuple_ extends Tuple, Value_>
        implements Indexer<Tuple_, Value_> {

    private final TriFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> submapFunction;
    private final Function<IndexProperties, IndexProperty_> startComparisonIndexPropertyFunction;
    private final Function<IndexProperties, IndexProperty_> endComparisonIndexPropertyFunction;
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>> comparisonMap = new TreeMap<>();
    private final NoneIndexer<Tuple_, Value_> invalidBucket = new NoneIndexer<>();

    public RangeIndexer(JoinerType startComparisonJoinerType, JoinerType endComparisonJoinerType,
            Function<IndexProperties, IndexProperty_> startComparisonIndexPropertyFunction,
            Function<IndexProperties, IndexProperty_> endComparisonIndexPropertyFunction,
            Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        if (startComparisonJoinerType != JoinerType.RANGE_GREATER_THAN ||
                endComparisonJoinerType != JoinerType.RANGE_LESS_THAN) {
            throw new IllegalArgumentException("Impossible state: joiners do not make a supported range [" +
                    startComparisonJoinerType + ", " + endComparisonJoinerType + "].");
        }
        BiFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> headSubmapFunction =
                getSubmapFunction(startComparisonJoinerType);
        BiFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> tailSubmapFunction =
                getSubmapFunction(endComparisonJoinerType);
        this.submapFunction = (comparisonMap, startComparisonIndexProperty, endComparisonIndexProperty) -> {
            NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>> tailSubMap =
                    tailSubmapFunction.apply(comparisonMap, endComparisonIndexProperty);
            if (tailSubMap.isEmpty()) {
                return Collections.emptyNavigableMap();
            }
            return headSubmapFunction.apply(tailSubMap, startComparisonIndexProperty);
        };
        this.startComparisonIndexPropertyFunction = Objects.requireNonNull(startComparisonIndexPropertyFunction);
        this.endComparisonIndexPropertyFunction = Objects.requireNonNull(endComparisonIndexPropertyFunction);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        IndexProperty_ startComparisonIndexProperty = startComparisonIndexPropertyFunction.apply(indexProperties);
        IndexProperty_ endComparisonIndexProperty = endComparisonIndexPropertyFunction.apply(indexProperties);
        int comparison = startComparisonIndexProperty.compareTo(endComparisonIndexProperty);
        if (comparison >= 0) { // Nothing good comes from this.
            invalidBucket.put(indexProperties, tuple, value);
            return;
        }
        Indexer<Tuple_, Value_> downstreamIndexer =
                comparisonMap.computeIfAbsent(startComparisonIndexProperty, k -> downstreamIndexerSupplier.get());
        downstreamIndexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        IndexProperty_ startComparisonIndexProperty = startComparisonIndexPropertyFunction.apply(indexProperties);
        IndexProperty_ endComparisonIndexProperty = endComparisonIndexPropertyFunction.apply(indexProperties);
        int comparison = startComparisonIndexProperty.compareTo(endComparisonIndexProperty);
        if (comparison >= 0) {
            return invalidBucket.remove(indexProperties, tuple);
        }
        Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(startComparisonIndexProperty);
        if (downstreamIndexer == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            comparisonMap.remove(startComparisonIndexProperty);
        }
        return value;
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleVisitor) {
        if (isEmpty()) {
            return;
        }
        IndexProperty_ startComparisonIndexProperty = startComparisonIndexPropertyFunction.apply(indexProperties);
        IndexProperty_ endComparisonIndexProperty = endComparisonIndexPropertyFunction.apply(indexProperties);
        int comparison = startComparisonIndexProperty.compareTo(endComparisonIndexProperty);
        if (comparison >= 0) {
            return;
        }
        Map<IndexProperty_, Indexer<Tuple_, Value_>> selectedComparisonMap =
                submapFunction.apply(comparisonMap, startComparisonIndexProperty, endComparisonIndexProperty);
        if (selectedComparisonMap.isEmpty()) {
            return;
        }
        for (Indexer<Tuple_, Value_> indexer : selectedComparisonMap.values()) {
            indexer.visit(indexProperties, tupleVisitor);
        }
    }

    @Override
    public boolean isEmpty() {
        return comparisonMap.isEmpty();
    }

}
