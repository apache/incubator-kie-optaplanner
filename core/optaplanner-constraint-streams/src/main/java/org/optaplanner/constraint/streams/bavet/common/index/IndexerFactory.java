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

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.JoinerType;

public class IndexerFactory {

    private final JoinerType[] joinerTypes;

    public IndexerFactory(AbstractJoiner joiner) {
        int joinerCount = joiner.getJoinerCount();
        joinerTypes = new JoinerType[joinerCount];
        for (int i = 0; i < joinerCount; i++) {
            JoinerType joinerType = joiner.getJoinerType(i);
            switch (joinerType) {
                case EQUAL:
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported joiner type (" + joinerType + ").");
            }
            joinerTypes[i] = joiner.getJoinerType(i);
        }
    }

    public <Tuple_ extends Tuple, Value_> Indexer<Tuple_, Value_> buildIndexer(boolean isLeftBridge) {
        if (joinerTypes.length == 0) {
            return new NoneIndexer<>();
        } else if (joinerTypes.length == 1) {
            JoinerType joinerType = joinerTypes[0];
            if (joinerType == JoinerType.EQUAL) {
                return new EqualsIndexer<>(s -> s.getIndexerKey(0, 1), NoneIndexer::new);
            } else {
                return new ComparisonIndexer<>(isLeftBridge ? joinerType : joinerType.flip(), s -> s.getProperty(0),
                        NoneIndexer::new);
            }
        }
        NavigableMap<Integer, JoinerType> joinerTypeMap = new TreeMap<>();
        for (int i = 1; i <= joinerTypes.length; i++) {
            JoinerType joinerType = i < joinerTypes.length ? joinerTypes[i] : null;
            JoinerType previousJoinerType = joinerTypes[i - 1];
            if (joinerType != JoinerType.EQUAL || previousJoinerType != joinerType) {
                joinerTypeMap.put(i, previousJoinerType);
            }
        }
        NavigableMap<Integer, JoinerType> descendingJoinerTypeMap = joinerTypeMap.descendingMap();
        Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier = NoneIndexer::new;
        for (Map.Entry<Integer, JoinerType> entry : descendingJoinerTypeMap.entrySet()) {
            Integer endingPropertyExclusive = entry.getKey();
            Integer previousEndingPropertyExclusive = descendingJoinerTypeMap.higherKey(endingPropertyExclusive);
            JoinerType joinerType = entry.getValue();
            Supplier<Indexer<Tuple_, Value_>> actualDownstreamIndexerSupplier = downstreamIndexerSupplier;
            if (joinerType == JoinerType.EQUAL) {
                downstreamIndexerSupplier = () -> new EqualsIndexer<>(
                        indexProperties -> indexProperties.getIndexerKey(
                                previousEndingPropertyExclusive == null ? 0 : previousEndingPropertyExclusive,
                                endingPropertyExclusive),
                        actualDownstreamIndexerSupplier);
            } else {
                downstreamIndexerSupplier = () -> new ComparisonIndexer<>(isLeftBridge ? joinerType : joinerType.flip(),
                        indexProperties -> indexProperties.getProperty(
                                previousEndingPropertyExclusive == null ? 0 : previousEndingPropertyExclusive),
                        actualDownstreamIndexerSupplier);
            }
        }
        return downstreamIndexerSupplier.get();
    }

}
