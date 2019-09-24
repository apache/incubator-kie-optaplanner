/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.stream;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import com.google.common.base.Functions;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.DefaultBiConstraintCollector;
import org.optaplanner.core.impl.score.stream.uni.DefaultUniConstraintCollector;

/**
 * Creates an {@link UniConstraintCollector}, {@link BiConstraintCollector}, ... instance
 * for use in {@link UniConstraintStream#groupBy(Function, UniConstraintCollector)}, ...
 */
public final class ConstraintCollectors {

    // ************************************************************************
    // count
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> count() {
        return countLong(Long::intValue);
    }

    public static <A> UniConstraintCollector<A, ?, Long> countLong() {
        return countLong(Functions.identity());
    }

    private static <A, B extends Number> UniConstraintCollector<A, ?, B> countLong(Function<Long, B> countConverter) {
        return new DefaultUniConstraintCollector<>(
                LongAdder::new,
                (resultContainer, a) -> {
                    resultContainer.increment();
                    return (resultContainer::decrement);
                },
                resultContainer -> countConverter.apply(resultContainer.longValue()));
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Integer> countBi() {
        return countLongBi(Long::intValue);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Long> countLongBi() {
        return countLongBi(Functions.identity());
    }

    private static <A, B, C extends Number> BiConstraintCollector<A, B, ?, C> countLongBi(Function<Long, C> countConverter) {
        return new DefaultBiConstraintCollector<>(
                LongAdder::new,
                (resultContainer, a, b) -> {
                    resultContainer.increment();
                    return (resultContainer::decrement);
                },
                resultContainer-> countConverter.apply(resultContainer.longValue()));
    }

    // ************************************************************************
    // countDistinct
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> countDistinct(Function<A, ?> groupValueMapping) {
        return ConstraintCollectors.countDistinctLong(groupValueMapping, Long::intValue);
    }

    public static <A> UniConstraintCollector<A, ?, Long> countDistinctLong(Function<A, ?> groupValueMapping) {
        return countDistinctLong(groupValueMapping, l -> l);
    }

    private static <A, B extends Number> UniConstraintCollector<A, ?, B> countDistinctLong(Function<A, ?> groupValueMapping,
            Function<Long, B> countConverter) {
        class CountDistinctResultContainer {
            long count = 0L;
            Map<Object, LongAdder> objectCountMap = new HashMap<>();
        }
        return new DefaultUniConstraintCollector<>(
                CountDistinctResultContainer::new,
                (resultContainer, a) -> {
                    Object value = groupValueMapping.apply(a);
                    LongAdder objectCount = resultContainer.objectCountMap.computeIfAbsent(value, k -> new LongAdder());
                    if (objectCount.longValue() == 0L) {
                        resultContainer.count++;
                    }
                    objectCount.increment();
                    return (() -> {
                        LongAdder objectCount2 = resultContainer.objectCountMap.get(value);
                        if (objectCount2 == null) {
                            throw new IllegalStateException("Impossible state: the value (" + value
                                    + ") of A (" + a + ") is removed more times than it was added.");
                        }
                        objectCount2.decrement();
                        if (objectCount2.longValue() == 0L) {
                            resultContainer.objectCountMap.remove(value);
                            resultContainer.count--;
                        }
                    });
                },
                resultContainer -> countConverter.apply(resultContainer.count));
    }

    // ************************************************************************
    // sum
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> sum(ToIntFunction<? super A> groupValueMapping) {
        return new DefaultUniConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a) -> {
                    int value = groupValueMapping.applyAsInt(a);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A> UniConstraintCollector<A, ?, Long> sumLong(ToLongFunction<? super A> groupValueMapping) {
        return new DefaultUniConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a) -> {
                    long value = groupValueMapping.applyAsLong(a);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Integer> sum(ToIntBiFunction<? super A, ? super B> groupValueMapping) {
        return new DefaultBiConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a, b) -> {
                    int value = groupValueMapping.applyAsInt(a, b);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Long> sumLong(ToLongBiFunction<? super A, ? super B> groupValueMapping) {
        return new DefaultBiConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a, b) -> {
                    long value = groupValueMapping.applyAsLong(a, b);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    // ************************************************************************
    // min
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, A> min(Comparator<A> comparator) {
        return minOrMax(comparator, true);
    }

    public static <A extends Comparable<A>> UniConstraintCollector<A, ?, A> min() {
        return min(Comparable::compareTo);
    }

    // ************************************************************************
    // max
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, A> max(Comparator<A> comparator) {
        return minOrMax(comparator, false);
    }

    public static <A extends Comparable<A>> UniConstraintCollector<A, ?, A> max() {
        return max(Comparable::compareTo);
    }

    private static <A> UniConstraintCollector<A, SortedMap<A, Long>, A> minOrMax(Comparator<A> comparator,
            boolean min) {
        Function<SortedMap<A, Long>, A> keySupplier = min ? SortedMap::firstKey : SortedMap::lastKey;
        return new DefaultUniConstraintCollector<>(
                () -> new TreeMap<>(comparator),
                (resultContainer, a) -> {
                    resultContainer.compute(a, (key, value) -> value == null ? 1 : value + 1);
                    return (() -> resultContainer.compute(a, (key, value) -> value == 1 ? null : value - 1));
                },
                (resultContainer) -> resultContainer.size() == 0 ? null : keySupplier.apply(resultContainer));
    }

    private ConstraintCollectors() {
    }

}
