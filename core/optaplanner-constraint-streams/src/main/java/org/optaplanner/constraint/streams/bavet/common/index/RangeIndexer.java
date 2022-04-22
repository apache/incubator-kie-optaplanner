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

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.JoinerType;

final class RangeIndexer<IndexProperty_ extends Comparable<IndexProperty_>, Tuple_ extends Tuple, Value_>
        implements Indexer<Tuple_, Value_> {

    private final TriFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> submapFunction;
    private final Function<IndexProperties, IndexProperty_> leftComparisonIndexPropertyFunction;
    private final Function<IndexProperties, IndexProperty_> rightComparisonIndexPropertyFunction;
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>> comparisonMap = new TreeMap<>();

    public RangeIndexer(JoinerType leftComparisonJoinerType, JoinerType rightComparisonJoinerType,
                        Function<IndexProperties, IndexProperty_> leftComparisonIndexPropertyFunction,
                        Function<IndexProperties, IndexProperty_> rightComparisonIndexPropertyFunction,
                        Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        BiFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> leftSubmapFunction
                = getSubmapFunction(leftComparisonJoinerType);
        BiFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> rightSubmapFunction
                = getSubmapFunction(rightComparisonJoinerType);
        this.submapFunction = (comparisonMap, leftComparisonIndexProperty, rightComparisonIndexProperty) -> {
            NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>> leftSubMap =
                    leftSubmapFunction.apply(comparisonMap, leftComparisonIndexProperty);
            return rightSubmapFunction.apply(leftSubMap, rightComparisonIndexProperty);
        };
        this.leftComparisonIndexPropertyFunction = Objects.requireNonNull(leftComparisonIndexPropertyFunction);
        this.rightComparisonIndexPropertyFunction = Objects.requireNonNull(rightComparisonIndexPropertyFunction);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Objects.requireNonNull(value);
        IndexProperty_ comparisonIndexProperty = leftComparisonIndexPropertyFunction.apply(indexProperties);
        Indexer<Tuple_, Value_> downstreamIndexer =
                comparisonMap.computeIfAbsent(comparisonIndexProperty, k -> downstreamIndexerSupplier.get());
        downstreamIndexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        IndexProperty_ comparisonIndexProperty = leftComparisonIndexPropertyFunction.apply(indexProperties);
        Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(comparisonIndexProperty);
        if (downstreamIndexer == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            comparisonMap.remove(comparisonIndexProperty);
        }
        return value;
    }

    @Override
    public void visit(IndexProperties indexProperties, Consumer<Map<Tuple_, Value_>> tupleValueMapVisitor) {
        IndexProperty_ leftComparisonIndexProperty = leftComparisonIndexPropertyFunction.apply(indexProperties);
        IndexProperty_ rightComparisonIndexProperty = rightComparisonIndexPropertyFunction.apply(indexProperties);
        Map<IndexProperty_, Indexer<Tuple_, Value_>> selectedComparisonMap =
                submapFunction.apply(comparisonMap, leftComparisonIndexProperty, rightComparisonIndexProperty);
        if (selectedComparisonMap.isEmpty()) {
            return;
        }
        for (Indexer<Tuple_, Value_> indexer : selectedComparisonMap.values()) {
            indexer.visit(indexProperties, tupleValueMapVisitor);
        }
    }

    @Override
    public boolean isEmpty() {
        return comparisonMap.isEmpty();
    }

}
