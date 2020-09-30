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

package org.optaplanner.core.impl.score.stream.drools.tri;

import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractAccumulateFunction;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;

public final class DroolsTriAccumulateFunction<A, B, C, ResultContainer_, NewA>
        extends DroolsAbstractAccumulateFunction<ResultContainer_, TriTuple<A, B, C>, NewA> {

    private final Supplier<ResultContainer_> supplier;
    private final QuadFunction<ResultContainer_, A, B, C, Runnable> accumulator;
    private final Function<ResultContainer_, NewA> finisher;

    public DroolsTriAccumulateFunction(TriConstraintCollector<A, B, C, ResultContainer_, NewA> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    public DroolsTriAccumulateFunction() {
        throw new UnsupportedOperationException("Serialization is not supported.");
    }

    @Override
    protected ResultContainer_ newContainer() {
        return supplier.get();
    }

    @Override
    protected Runnable accumulate(ResultContainer_ container, TriTuple<A, B, C> tuple) {
        return accumulator.apply(container, tuple.a, tuple.b, tuple.c);
    }

    @Override
    protected NewA getResult(ResultContainer_ container) {
        return finisher.apply(container);
    }
}
