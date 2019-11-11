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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public class GroupByAcc<A, B, ResultContainer, NewB> {
    private final Map<ResultContainer, Long> useCount = new HashMap<>(0);
    private final Map<A, ResultContainer> containers = new HashMap<>(0);
    private final Supplier<ResultContainer> supplier;
    private final BiFunction<ResultContainer, B, Runnable> accumulator;
    private final Function<ResultContainer, NewB> finisher;

    public GroupByAcc(final UniConstraintCollector<B, ResultContainer, NewB> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    public Runnable accumulate(A key, B value) {
        ResultContainer container = containers.computeIfAbsent(key, k -> supplier.get());
        Runnable undo = accumulator.apply(container, value);
        useCount.compute(container, (k, count) -> count == null ? 1 : count + 1); // Increment use counter.
        return () -> {
            undo.run();
            // Decrement use counter. If 0, container is ignored. This prevents empty groups from showing up in results.
            useCount.compute(container, (k, count) -> count == 1 ? null : count - 1);
        };
    }

    public List<GroupByAcc.Pair<A, NewB>> finish() {
        List<Pair<A, NewB>> results = new ArrayList<>(useCount.size());
        for (Map.Entry<A, ResultContainer> entry: containers.entrySet()) {
            ResultContainer container = entry.getValue();
            if (useCount.containsKey(container)) {
                results.add(new Pair<>(entry.getKey(), finisher.apply(container)));
            }
        }
        return results;
    }

    public static class Pair<K,V> {
        public final K key;
        public final V value;

        public Pair( K key, V value ) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

    }

}
