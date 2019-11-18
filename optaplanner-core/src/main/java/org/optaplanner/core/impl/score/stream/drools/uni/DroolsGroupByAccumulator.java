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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public final class DroolsGroupByAccumulator<A, B, ResultContainer, NewB> implements Serializable {

    private final Map<ResultContainer, Long> containersInUse = new HashMap<>(0);
    private final Map<A, ResultContainer> containers = new HashMap<>(0);
    private final Supplier<ResultContainer> supplier;
    private final BiFunction<ResultContainer, B, Runnable> accumulator;
    private final Function<ResultContainer, NewB> finisher;

    public DroolsGroupByAccumulator(final UniConstraintCollector<B, ResultContainer, NewB> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    private static Long increment(Long count) {
        return count == null ? 1L : count + 1L;
    }

    private static Long decrement(Long count) {
        return count == 1L ? null : count - 1L;
    }

    public Runnable accumulate(A key, B value) {
        ResultContainer container = containers.computeIfAbsent(key, __ -> supplier.get());
        Runnable undo = accumulator.apply(container, value);
        containersInUse.compute(container, (__, count) -> increment(count)); // Increment use counter.
        return () -> {
            undo.run();
            // Decrement use counter. If 0, container is ignored during finishing. Removes empty groups from results.
            Long currentCount = containersInUse.compute(container, (__, count) -> decrement(count));
            if (currentCount == null) {
                containers.remove(key);
            }
        };
    }

    public Set<DroolsGroupByAccumulator.Pair<A, NewB>> finish() {
        if (containers.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Pair<A, NewB>> results = new LinkedHashSet<>(containers.size());
        for (Map.Entry<A, ResultContainer> entry: containers.entrySet()) {
            ResultContainer container = entry.getValue();
            results.add(new DroolsGroupByAccumulator.Pair<>(entry.getKey(), finisher.apply(container)));
        }
        return results;
    }

    public static final class Pair<K,V> {
        public final K key;
        public final V value;
        private final int hashCode;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
            this.hashCode = Objects.hash(key, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !Objects.equals(getClass(), o.getClass())) {
                return false;
            }
            final DroolsGroupByAccumulator.Pair<?, ?> pair = (DroolsGroupByAccumulator.Pair<?, ?>) o;
            return Objects.equals(key, pair.key) &&
                    Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "Pair[" + key + ", " + value + "]";
        }
    }

}
