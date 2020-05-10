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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractUniCollectingGroupByCollectorProcessor;

final class DroolsBiGroupByCollectorProcessor<A, B, ResultContainer, NewA, NewB>
        extends
        DroolsAbstractUniCollectingGroupByCollectorProcessor<ResultContainer, BiTuple<A, B>, NewA, BiTuple<NewA, NewB>> {

    private final BiFunction<A, B, NewA> groupKeyMapping;
    private final Supplier<ResultContainer> supplier;
    private final TriFunction<ResultContainer, A, B, Runnable> accumulator;
    private final Function<ResultContainer, NewB> finisher;

    public DroolsBiGroupByCollectorProcessor(BiFunction<A, B, NewA> groupKeyMapping,
            BiConstraintCollector<A, B, ResultContainer, NewB> collector) {
        this.groupKeyMapping = groupKeyMapping;
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    @Override
    protected NewA toKey(BiTuple<A, B> tuple) {
        return groupKeyMapping.apply(tuple.a, tuple.b);
    }

    @Override
    protected ResultContainer newContainer() {
        return supplier.get();
    }

    @Override
    protected Runnable process(BiTuple<A, B> tuple, ResultContainer container) {
        return accumulator.apply(container, tuple.a, tuple.b);
    }

    @Override
    protected BiTuple<NewA, NewB> toResult(NewA key, ResultContainer container) {
        return new BiTuple<>(key, finisher.apply(container));
    }

}
