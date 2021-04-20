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
import org.drools.core.reteoo.SubnetworkTuple;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;
import org.drools.model.Variable;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

final class UniAccumulator<A, ResultContainer_, Result_> implements Accumulator {

    private final String varA;
    private final UniConstraintCollector<A, ResultContainer_, Result_> collector;

    private boolean subnetwork;
    private Declaration declrA;
    private int offsetToA;

    public UniAccumulator(Variable<A> varA, UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        this.varA = varA.getName();
        this.collector = collector;
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

    private InternalFactHandle getFactHandle(Tuple leftTuple, InternalFactHandle handle,
            Declaration[] innerDeclarations) {
        if (declrA == null) {
            return init(leftTuple, handle, innerDeclarations);
        } else if (!subnetwork) {
            return handle;
        } else {
            return AccumulatorUtils.getTuple(offsetToA, leftTuple).getFactHandle();
        }
    }

    private InternalFactHandle init(Tuple leftTuple, InternalFactHandle handle, Declaration[] innerDeclarations) {
        for (Declaration declr : innerDeclarations) {
            if (declr.getBindingName().equals(varA)) {
                declrA = declr;
                break;
            }
        }

        subnetwork = (leftTuple instanceof SubnetworkTuple);
        if (!subnetwork) {
            return handle;
        } else {
            AccumulatorUtils.OffsetHolder holder = new AccumulatorUtils.OffsetHolder();
            Tuple tuple = leftTuple;
            tuple = AccumulatorUtils.findTupleAndOffset(tuple, holder, declrA);
            offsetToA = holder.offset;
            return tuple.getFactHandle();
        }
    }

    @Override
    public Object accumulate(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle,
            Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) {
        InternalFactHandle factHandle = getFactHandle(leftTuple, handle, innerDeclarations);
        A a = (A) declrA.getValue(null, factHandle.getObject());
        Runnable undo = collector.accumulator().apply((ResultContainer_) context, a);
        return undo;
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
