/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupByAccumulator;

final class DroolsUniGroupByAccumulator<A, B, ResultContainer, NewB>
        extends DroolsAbstractGroupByAccumulator<ResultContainer, BiTuple<A, B>, A, BiTuple<A, NewB>> {

    private final Supplier<ResultContainer> supplier;
    private final BiFunction<ResultContainer, B, Runnable> accumulator;
    private final Function<ResultContainer, NewB> finisher;

    public DroolsUniGroupByAccumulator(UniConstraintCollector<B, ResultContainer, NewB> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    @Override
    protected A toKey(BiTuple<A, B> abBiTuple) {
        return abBiTuple._1;
    }

    @Override
    protected ResultContainer newContainer() {
        return supplier.get();
    }

    @Override
    protected Runnable process(BiTuple<A, B> abBiTuple, ResultContainer container) {
        return accumulator.apply(container, abBiTuple._2);
    }

    @Override
    protected BiTuple<A, NewB> toResult(A key, ResultContainer container) {
        return new BiTuple<>(key, finisher.apply(container));
    }
}
