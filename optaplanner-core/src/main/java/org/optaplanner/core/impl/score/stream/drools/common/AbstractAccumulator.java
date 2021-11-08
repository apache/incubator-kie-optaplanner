/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;

abstract class AbstractAccumulator<ResultContainer_, Result_> implements Accumulator {

    private static final UnaryOperator<Tuple> TUPLE_EXTRACTOR_OFFSET_0 = tuple -> tuple;
    private static final UnaryOperator<Tuple> TUPLE_EXTRACTOR_OFFSET_1 = Tuple::getParent;
    private static final UnaryOperator<Tuple> TUPLE_EXTRACTOR_OFFSET_2 = tuple -> tuple.getParent()
            .getParent();
    private static final UnaryOperator<Tuple> TUPLE_EXTRACTOR_OFFSET_3 = tuple -> tuple.getParent()
            .getParent()
            .getParent();

    private final Supplier<ResultContainer_> containerSupplier;
    private final Function<ResultContainer_, Result_> finisher;

    protected AbstractAccumulator(Supplier<ResultContainer_> containerSupplier,
            Function<ResultContainer_, Result_> finisher) {
        this.containerSupplier = Objects.requireNonNull(containerSupplier);
        this.finisher = Objects.requireNonNull(finisher);
    }

    protected static <Value_> Value_ extractValue(Declaration declaration, Tuple extractedTuple) {
        return (Value_) declaration.getValue(null, extractedTuple.getFactHandle().getObject());
    }

    protected static UnaryOperator<Tuple> getTupleExtractor(Declaration declaration, Tuple tuple) {
        int offset = 0;
        while (tuple.getIndex() != declaration.getTupleIndex()) {
            tuple = tuple.getParent();
            offset++;
        }
        switch (offset) {
            case 0:
                return TUPLE_EXTRACTOR_OFFSET_0;
            case 1:
                return TUPLE_EXTRACTOR_OFFSET_1;
            case 2:
                return TUPLE_EXTRACTOR_OFFSET_2;
            case 3:
                return TUPLE_EXTRACTOR_OFFSET_3;
            default:
                throw new UnsupportedOperationException("Impossible state: tuple delta offset (" + offset + ").");
        }
    }

    @Override
    public final Object createWorkingMemoryContext() {
        return null;
    }

    @Override
    public final Object createContext() {
        return null; // We always create and init during init(...).
    }

    @Override
    public final ResultContainer_ init(Object workingMemoryContext, Object context, Tuple leftTuple,
            Declaration[] declarations, ReteEvaluator reteEvaluator) {
        return containerSupplier.get();
    }

    @Override
    public final boolean supportsReverse() {
        return true;
    }

    @Override
    public final boolean tryReverse(Object workingMemoryContext, Object context, Tuple leftTuple,
            InternalFactHandle handle, Object value, Declaration[] declarations, Declaration[] innerDeclarations,
            ReteEvaluator reteEvaluator) {
        ((Runnable) value).run();
        return true;
    }

    @Override
    public Result_ getResult(Object workingMemoryContext, Object context, Tuple leftTuple, Declaration[] declarations,
            ReteEvaluator reteEvaluator) {
        return finisher.apply((ResultContainer_) context);
    }
}
