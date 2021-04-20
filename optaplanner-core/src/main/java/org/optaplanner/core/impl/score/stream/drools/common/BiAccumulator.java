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

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;
import org.drools.model.Variable;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

final class BiAccumulator<A, B, ResultContainer_, Result_> implements Accumulator {

    private final String varA;
    private final String varB;
    private final BiConstraintCollector<A, B, ResultContainer_, Result_> collector;

    private Declaration declrA;
    private Declaration declrB;

    private int offsetToB;
    private int offsetToA;

    public BiAccumulator(Variable<A> varA, Variable<B> varB,
            BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        this.collector = collector;
        this.varA = varA.getName();
        this.varB = varB.getName();
    }

    @Override
    public Object createWorkingMemoryContext() {
        return null;
    }

    @Override
    public Object createContext() {
        return null; // We always create and init during init(...).
    }

    @Override
    public Object init(Object workingMemoryContext, Object context, Tuple leftTuple, Declaration[] declarations,
            WorkingMemory workingMemory) {
        return collector.supplier().get();
    }

    @Override
    public Object accumulate(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle,
            Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) {
        if (declrA == null) {
            return initAndAccumulate((ResultContainer_) context, leftTuple, innerDeclarations);
        }

        Tuple tuple = leftTuple;
        tuple = AccumulatorUtils.getTuple(offsetToB, tuple);
        B b = (B) declrB.getValue(null, tuple.getFactHandle().getObject());
        tuple = AccumulatorUtils.getTuple(offsetToA, tuple);
        A a = (A) declrA.getValue(null, tuple.getFactHandle().getObject());

        Runnable undo = collector.accumulator().apply((ResultContainer_) context, a, b);
        return undo;
    }

    private Runnable initAndAccumulate(ResultContainer_ context, Tuple leftTuple, Declaration[] innerDeclarations) {
        for (Declaration declr : innerDeclarations) {
            if (declr.getBindingName().equals(varA)) {
                declrA = declr;
            } else if (declr.getBindingName().equals(varB)) {
                declrB = declr;
            }
        }

        AccumulatorUtils.OffsetHolder holder = new AccumulatorUtils.OffsetHolder();

        Tuple tuple = leftTuple;
        tuple = AccumulatorUtils.findTupleAndOffset(tuple, holder, declrB);
        offsetToB = holder.offset;
        B b = (B) declrB.getValue(null, tuple.getFactHandle().getObject());

        tuple = AccumulatorUtils.findTupleAndOffset(tuple, holder, declrA);
        offsetToA = holder.offset;
        A a = (A) declrA.getValue(null, tuple.getFactHandle().getObject());

        return collector.accumulator().apply(context, a, b);
    }

    @Override
    public boolean supportsReverse() {
        return true;
    }

    @Override
    public boolean tryReverse(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle,
            Object value, Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) {
        if (value != null) {
            ((Runnable) value).run();
        }
        return true;
    }

    @Override
    public Object getResult(Object workingMemoryContext, Object context, Tuple leftTuple, Declaration[] declarations,
            WorkingMemory workingMemory) {
        return collector.finisher().apply((ResultContainer_) context);
    }
}