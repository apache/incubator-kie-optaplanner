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

    private final boolean allowsSameLeftAndRight;
    private final TriFunction<NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>, IndexProperty_, IndexProperty_, NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>>> submapFunction;
    private final Function<IndexProperties, IndexProperty_> leftComparisonIndexPropertyFunction;
    private final Function<IndexProperties, IndexProperty_> rightComparisonIndexPropertyFunction;
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final NavigableMap<IndexProperty_, Indexer<Tuple_, Value_>> comparisonMap = new TreeMap<>();
    private final NoneIndexer<Tuple_, Value_> invalidBucket = new NoneIndexer<>();

    public RangeIndexer(JoinerType leftComparisonJoinerType, JoinerType rightComparisonJoinerType,
                        Function<IndexProperties, IndexProperty_> leftComparisonIndexPropertyFunction,
                        Function<IndexProperties, IndexProperty_> rightComparisonIndexPropertyFunction,
                        Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        // Ensure the range.
        boolean validRange = false;
        if (leftComparisonJoinerType == JoinerType.GREATER_THAN || leftComparisonJoinerType == JoinerType.GREATER_THAN_OR_EQUAL) {
            if (rightComparisonJoinerType == JoinerType.LESS_THAN || rightComparisonJoinerType == JoinerType.LESS_THAN_OR_EQUAL) {
                validRange = true;
            }
        } else if (leftComparisonJoinerType == JoinerType.LESS_THAN || leftComparisonJoinerType == JoinerType.LESS_THAN_OR_EQUAL) {
            if (rightComparisonJoinerType == JoinerType.GREATER_THAN || rightComparisonJoinerType == JoinerType.GREATER_THAN_OR_EQUAL) {
                validRange = true;
            }
        }
        if (!validRange) {
            throw new IllegalArgumentException("Impossible state: joiners do not make a range [" +
                    leftComparisonJoinerType + ", " + rightComparisonJoinerType + "].");
        }
        /*
         * Index properties [X,X] can only be allowed if both left and right ranges are inclusive.
         * In case of exclusive ranges, no X exists that X > Y while X < Y.
         * However, the index still needs to behave properly in this situation during removal.
         * In this case, the tuple goes into a separate "invalid" bucket that is not used during visitation.
         */
        allowsSameLeftAndRight = (leftComparisonJoinerType == JoinerType.GREATER_THAN_OR_EQUAL || leftComparisonJoinerType == JoinerType.LESS_THAN_OR_EQUAL)
            && (rightComparisonJoinerType == JoinerType.GREATER_THAN_OR_EQUAL || rightComparisonJoinerType == JoinerType.LESS_THAN_OR_EQUAL);
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
        IndexProperty_ leftComparisonIndexProperty = leftComparisonIndexPropertyFunction.apply(indexProperties);
        if (!allowsSameLeftAndRight) {
            IndexProperty_ rightComparisonIndexProperty = rightComparisonIndexPropertyFunction.apply(indexProperties);
            if (leftComparisonIndexProperty.equals(rightComparisonIndexProperty)) {
                invalidBucket.put(indexProperties, tuple, value);
                return;
            }
        }
        Indexer<Tuple_, Value_> downstreamIndexer =
                comparisonMap.computeIfAbsent(leftComparisonIndexProperty, k -> downstreamIndexerSupplier.get());
        downstreamIndexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        IndexProperty_ leftComparisonIndexProperty = leftComparisonIndexPropertyFunction.apply(indexProperties);
        if (!allowsSameLeftAndRight) {
            IndexProperty_ rightComparisonIndexProperty = rightComparisonIndexPropertyFunction.apply(indexProperties);
            if (leftComparisonIndexProperty.equals(rightComparisonIndexProperty)) {
                return invalidBucket.remove(indexProperties, tuple);
            }
        }
        Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(leftComparisonIndexProperty);
        if (downstreamIndexer == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer.");
        }
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            comparisonMap.remove(leftComparisonIndexProperty);
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
